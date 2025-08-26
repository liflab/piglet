package ca.uqac.lif.codefinder.assertion;

import java.util.Set;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;


/**
 * Finds assertions containing a call to method Object.equals.
 */
public class EqualityWithMessageFinder extends AssertionFinder
{	
	public EqualityWithMessageFinder(String filename)
	{
		super("With text message", filename);
	}

	@Override
	public AssertionFinder newFinder(String filename)
	{
		return new EqualityWithMessageFinder(filename);
	}

	@Override
	public void visit(MethodCallExpr n, Set<FoundToken> set)
	{
		super.visit(n, set);
		if (isAssertionEquals(n) && hasMessage(n))
		{
			set.add(new EqualityWithMessageToken(m_filename, n.getBegin().get().line, n.toString()));
		}
	}

	protected static boolean hasMessage(MethodCallExpr n)
	{
		if (n.getArguments().size() != 3)
		{
			return false;
		}
		try
		{
			ResolvedType type1 = n.getArgument(0).calculateResolvedType();
			return type1.describe().compareTo("java.lang.String") == 0;
		}
		catch (Exception e)
		{
			// Unable to resolve type
			// Ignore for the moment
			return false;
		}
	}

	public class EqualityWithMessageToken extends FoundToken
	{
		public EqualityWithMessageToken(String filename, int line, String snippet)
		{
			super(filename, line, line, snippet);
		}		

		@Override
		public String getAssertionName()
		{
			return EqualityWithMessageFinder.this.getName();
		}
	}
}
