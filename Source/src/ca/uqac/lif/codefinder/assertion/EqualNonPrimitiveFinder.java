package ca.uqac.lif.codefinder.assertion;

import java.util.Set;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;

public class EqualNonPrimitiveFinder extends AssertionFinder
{
	public EqualNonPrimitiveFinder(String filename)
	{
		super("Equality between non-primitive values", filename);
	}
	
	@Override
	public AssertionFinder newFinder(String filename)
	{
		return new EqualNonPrimitiveFinder(filename);
	}
	
	@Override
	public void visit(MethodCallExpr n, Set<FoundToken> set)
	{
		super.visit(n, set);
		if (isAssertionEquals(n) && hasNonPrimitive(n))
		{
			set.add(new EqualNonPrimitiveToken(m_filename, n.getBegin().get().line, n.toString()));
		}
	}
	
	protected static boolean hasNonPrimitive(MethodCallExpr n)
	{
		if (n.getArguments().size() < 2)
		{
			return false;
		}
		try
		{
			ResolvedType type1 = n.getArgument(0).calculateResolvedType();
			ResolvedType type2 = n.getArgument(1).calculateResolvedType();
			if (!isPrimitive(type1) && !isPrimitive(type2))
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
	
	protected static boolean isPrimitive(ResolvedType t)
	{
		/*if (t.isPrimitive())
		{
			return true;
		}*/
		String type_name = t.describe();
		if (/* type_name.equals("java.lang.String") || */ type_name.equals("java.lang.Integer") || type_name.equals("java.lang.Long")
				|| type_name.equals("java.lang.Float") || type_name.equals("java.lang.Double") || type_name.equals("java.lang.Byte")
				|| type_name.equals("java.lang.Short") || type_name.equals("java.lang.Character") || type_name.equals("java.lang.Boolean"))
		{
			return true;
		}
		return false;
	}
	
	public class EqualNonPrimitiveToken extends FoundToken
	{
		public EqualNonPrimitiveToken(String filename, int line, String snippet)
		{
			super(filename, line, line, snippet);
		}
		
		@Override
		public String getAssertionName()
		{
			return EqualNonPrimitiveFinder.this.getName();
		}
	}

}
