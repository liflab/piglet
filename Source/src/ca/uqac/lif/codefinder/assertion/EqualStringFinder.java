package ca.uqac.lif.codefinder.assertion;

import java.util.Set;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;

public class EqualStringFinder extends AssertionFinder
{
	public EqualStringFinder(String filename)
	{
		super("Equality between strings", filename);
	}
	
	@Override
	public AssertionFinder newFinder(String filename)
	{
		return new EqualStringFinder(filename);
	}
	
	@Override
	public void visit(MethodCallExpr n, Set<FoundToken> set)
	{
		super.visit(n, set);
		if (isAssertionEquals(n) && comparesStrings(n))
		{
			set.add(new EqualStringToken(m_filename, n.getBegin().get().line, n.toString()));
		}
	}
	
	/**
	 * Determines whether the given method call expression compares two strings.
	 * @param n The method call expression to examine
	 * @return true if the method call compares two strings, false otherwise
	 */
	protected static boolean comparesStrings(MethodCallExpr n)
	{
		if (n.getArguments().size() < 2)
		{
			return false;
		}
		try
		{
			ResolvedType type1 = n.getArgument(0).calculateResolvedType();
			ResolvedType type2 = n.getArgument(1).calculateResolvedType();
			if (type1.describe().equals("java.lang.String") && type2.describe().equals("java.lang.String"))
			{
				return true;
			}
		}
		catch (Exception e)
		{
			// Unable to resolve type
			// Ignore for the moment
			return false;
		}
		return false;
	}
	
	public class EqualStringToken extends FoundToken
	{
		public EqualStringToken(String filename, int line, String snippet)
		{
			super(filename, line, line, snippet);
		}
		
		@Override
		public String getAssertionName()
		{
			return EqualStringFinder.this.getName();
		}
	}

}
