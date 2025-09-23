/*
    Analysis of assertions in Java programs
    Copyright (C) 2025 Sylvain Hallé, Sarika Machhindra Kadam

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.piglet.find.sparql;

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
  public QueryIterator execEvaluated(Binding input,
                                     Node subject,
                                     Node predicate,
                                     Node object,
                                     ExecutionContext execCxt) {
    // Subject may be a Var or a concrete IRI
    final Var sVar = subject.isVariable() ? Var.alloc(subject) : null;
    final Var oVar = object.isVariable() ? Var.alloc(object) : null;

    final List<Binding> out = new ArrayList<>();

    // Helper: attempt a single (iri, maybeObjectVar)/(iri, constantObject) case
    java.util.function.BiConsumer<String, Binding> handleOne = (iri, in) -> {
      U value = calculate(iri);
      if (value == null) return; // unknown subject or failed compute

      // What to do with the object position?
      if (oVar != null) {
        // Object is a variable: bind it to a literal of the computed value
        Node valNode = NodeFactory.createLiteralString(value.toString());
        // Respect existing binding compatibility
        if (in.contains(oVar) && !in.get(oVar).equals(valNode)) return;
        BindingBuilder bb = BindingFactory.builder(in);
        bb.add(oVar, valNode);
        out.add(bb.build());
      } else {
        // Object is a constant: compare
        if (!object.isLiteral()) return; // only literal supported here
        String want = object.getLiteralLexicalForm();
        if (!want.equals(value.toString())) return; // no match ⇒ no binding
        // Match: keep binding as-is
        out.add(in);
      }
    };

    if (sVar != null) {
      // Subject is a variable: iterate all known IRIs
      for (String iri : idx.allIris()) {
        // Respect existing binding compatibility
        if (input.contains(sVar) && !input.get(sVar).toString().equals(iri)) continue;
        BindingBuilder bb = BindingFactory.builder(input);
        Node iriNode = NodeFactory.createURI(iri);
        bb.add(sVar, iriNode);
        handleOne.accept(iri, bb.build());
      }
    } else {
      // Subject is concrete
      if (!subject.isURI()) return IterLib.noResults(execCxt);
      String iri = subject.getURI();
      handleOne.accept(iri, input);
    }

    if (out.isEmpty()) return IterLib.noResults(execCxt);
    return QueryIterPlainWrapper.create(out.iterator(), execCxt);
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
