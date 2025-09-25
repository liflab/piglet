package ca.uqac.lif.piglet.find.sparql;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.pfunction.PFuncSimple;
import org.apache.jena.sparql.util.IterLib;

import com.github.javaparser.resolution.TypeSolver;

import ca.uqac.lif.piglet.util.TypeRdf;

/**
 * A function that determines if a given type name is a descendant of
 * another type. The function takes two arguments: the first is the
 * type to check, and the second is the supposed ancestor type.
 */
public class InstanceOf extends PFuncSimple
{
	private final TypeSolver m_ts;

	public InstanceOf(TypeSolver ts)
	{
		super();
		m_ts = ts;
	}

	@Override
	public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, Node object, ExecutionContext execCxt)
	{
		// Resolve RDF node -> internal Type
		String t_subject = trimQuotes(subject.toString());
		String t_object = trimQuotes(object.toString());
		if (TypeRdf.isSubtypeOf(t_object, t_subject, m_ts)) {
			return IterLib.result(binding, execCxt);
		} else {
			return IterLib.noResults(execCxt);
		}
	}

	private static String trimQuotes(String s)
	{
		if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2)
		{
			return s.substring(1, s.length() - 1);
		}
		return s;
	}
}
