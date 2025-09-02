package ca.uqac.lif.codefinder.assertion;

import com.github.javaparser.ast.expr.MethodCallExpr;

public class NonFluentAssertionsCounter extends AssertionCounter
{
	public NonFluentAssertionsCounter(String filename)
	{
		super("Non-fluent assertions", filename);
	}
	
	@Override
	public void visit(MethodCallExpr n, Void v)
	{
		super.visit(n, v);
		if (isNonFluentAssertion(n))
		{
			addToken(n);
		}
	}

	@Override
	public NonFluentAssertionsCounter newFinder(String filename)
	{
		return new NonFluentAssertionsCounter(filename);
	}
}
