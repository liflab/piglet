package ca.uqac.lif.codefinder.find.sparql;

import com.github.javaparser.ast.expr.Expression;

public abstract class JavaAstNodeFunction extends LazyNodeFunction<Expression, String>
{
	public JavaAstNodeFunction(LazyNodeIndex<Expression, String> idx)
	{
		super(idx);
	}
}
