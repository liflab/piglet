package ca.uqac.lif.piglet.find.sparql;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.resolution.TypeSolver;

import ca.uqac.lif.piglet.util.TypeRdf;

/**
 * A function that resolves the type of an expression.
 * The function takes as argument an expression and returns a string
 * representation of its type.
 */
public class ResolveType extends JavaAstNodeFunction
{
	private final TypeSolver m_ts;
	
	public ResolveType(LazyNodeIndex<Node,String> idx, TypeSolver ts)
	{
		super(idx);
		m_ts = ts;
	}

	@Override
	protected String calculateValue(Node n)
	{
		if (n instanceof ClassOrInterfaceDeclaration || n instanceof Expression)
		{
			return TypeRdf.resolveTypeToString(n, m_ts);
		}
		return "?";
		
		/*
		ResolveResult<ResolvedType> rr = Types.typeOfWithTimeout((Expression) n, m_ts, 100);
		if (rr.reason == Types.ResolveReason.RESOLVED)
		{
			//System.out.println("Resolved type of " + n + " to " + rr.value.get().describe());
			return rr.value.get().describe();
		}
		return "?";
		*/
	}
}
