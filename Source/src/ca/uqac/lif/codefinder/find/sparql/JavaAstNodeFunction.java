package ca.uqac.lif.codefinder.find.sparql;

import com.github.javaparser.ast.Node;

public abstract class JavaAstNodeFunction extends LazyNodeFunction<com.github.javaparser.ast.Node, String>
{
	public JavaAstNodeFunction(LazyNodeIndex<Node, String> idx)
	{
		super(idx);
	}
}
