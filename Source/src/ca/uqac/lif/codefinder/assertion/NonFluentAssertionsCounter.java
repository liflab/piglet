package ca.uqac.lif.codefinder.assertion;

import java.util.Set;

import com.github.javaparser.ast.expr.MethodCallExpr;

public class NonFluentAssertionsCounter extends AssertionCounter
{
	public NonFluentAssertionsCounter(String filename)
	{
		super("Non-fluent assertions", filename);
	}
	
	@Override
	public void visit(MethodCallExpr n, Set<FoundToken> set)
	{
		super.visit(n, set);
		if (isNonFluentAssertion(n))
		{
			set.add(new FoundToken(m_name, m_filename, n.getBegin().get().line, n.getEnd().get().line, n.toString()));
		}
	}
}
