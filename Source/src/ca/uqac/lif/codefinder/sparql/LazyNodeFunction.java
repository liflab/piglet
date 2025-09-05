package ca.uqac.lif.codefinder.sparql;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.pfunction.PFuncSimple;
import org.apache.jena.sparql.util.IterLib;

public abstract class LazyNodeFunction<T,U> extends PFuncSimple
{
	private final LazyNodeIndex<T,U> idx;
	
	public LazyNodeFunction(LazyNodeIndex<T,U> idx)
	{
		this.idx = idx;
	}
	
  @Override
  public QueryIterator execEvaluated(Binding binding, Node s, Node p, Node o, ExecutionContext cxt) {
    // If ?x is unbound (optimizer may call PF before other triples), generate candidates
    if (Var.isVar(s)) {
      Var sVar = Var.alloc(s);
      boolean oIsVar = Var.isVar(o);
      Var oVar = oIsVar ? Var.alloc(o) : null;

      List<Binding> out = new ArrayList<>();
      for (String iri : idx.allIris()) {                 // iterate your known AST nodes
        Node subj = NodeFactory.createURI(iri);
        U value = calculate(iri);
        if (value == null) continue;
        Node type = NodeFactory.createURI(value.toString());                    // lazily compute/cache inside idx

        if (!oIsVar && !o.equals(type)) continue;        // respect a bound object

        BindingBuilder bb = BindingFactory.builder(binding);
        bb.add(sVar, subj);
        if (oIsVar) bb.add(oVar, type);
        out.add(bb.build());
      }
      return QueryIterPlainWrapper.create(out.iterator(), cxt);
    }

    // Subject bound
    if (!s.isURI()) return IterLib.noResults(cxt);       // adapt if you use blank nodes
    U value = calculate(s.getURI());
    if (value == null) return IterLib.noResults(cxt);
    Node type = NodeFactory.createURI(value.toString());

    if (Var.isVar(o)) {
      return IterLib.result(BindingFactory.binding(binding, Var.alloc(o), type), cxt);
    } else {
      return o.equals(type) ? IterLib.result(binding, cxt) : IterLib.noResults(cxt);
    }
  }
  
  protected U calculate(String iri)
	{
  	// First check cache
  	if (idx.typeCache.containsKey(iri))
			return idx.typeCache.get(iri);
  	// Not in cache, compute it
		T astNode = idx.get(iri);
		if (astNode == null)
			return null;
		U value = calculateValue(astNode);
		// Store in cache
		idx.typeCache.put(iri, value);
		return value;
	}
	
	protected abstract U calculateValue(T astNode);
}
