package ca.uqac.lif.codefinder.assertion;

import java.util.Set;

import com.github.javaparser.ast.expr.MethodCallExpr;

/**
 * Finds all assertions in a Java file.
 */
public class AnyAssertionFinder extends AssertionFinder
{	
	public AnyAssertionFinder(String filename)
	{
		super("All assertions", filename);
	}
	
	@Override
	public AssertionFinder newFinder(String filename)
	{
		return new AnyAssertionFinder(filename);
	}

	@Override
	public void visit(MethodCallExpr n, Set<FoundToken> set)
	{
		super.visit(n, set);
		if (isAssertion(n))
		{
			set.add(new AnyAssertionToken(m_filename, n.getBegin().get().line, n.toString()));
		}
	}

	public class AnyAssertionToken extends FoundToken
	{
		public AnyAssertionToken(String filename, int line, String snippet)
		{
			super(filename, line, line, snippet);
		}

		@Override
		public String getAssertionName()
		{
			return AnyAssertionFinder.this.getName();
		}

	}
}
