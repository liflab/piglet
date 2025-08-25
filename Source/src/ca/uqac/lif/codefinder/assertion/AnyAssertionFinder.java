package ca.uqac.lif.codefinder.assertion;

import java.util.List;
import java.util.Set;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;


/**
 * Finds assertions containing a call to method Object.equals.
 */
public class AnyAssertionFinder extends AssertionFinder
{	
	public AnyAssertionFinder(String filename)
	{
		super(filename);
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
	
	public static class EqualAssertionToken extends FoundToken
	{
		public EqualAssertionToken(String filename, int line, String snippet)
		{
			super(filename, line, line, snippet);
		}		
	}

	public static class AnyAssertionToken extends FoundToken
	{
		public AnyAssertionToken(String filename, int line, String snippet)
		{
			super(filename, line, line, snippet);
		}

	}
}
