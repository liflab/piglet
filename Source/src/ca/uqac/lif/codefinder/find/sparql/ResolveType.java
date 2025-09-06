package ca.uqac.lif.codefinder.find.sparql;

import com.github.javaparser.ast.Node;

public class ResolveType extends JavaAstNodeFunction
{
	public ResolveType(LazyNodeIndex<Node, String> idx)
	{
		super(idx);
	}

	@Override
	protected String calculateValue(Node n)
	{
		return "java.lang.int"; // Placeholder
	}
}
