/*
    Analysis of assertions in Java programs
    Copyright (C) 2025 Sylvain Hallé, Sarika Machhindra Kadam

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.codefinder.assertion;

import java.util.Set;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;

/**
 * Finds assertions that compare non-primitive values using equality.
 */
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
	
	/**
	 * Determines if at least one of the two arguments of the method call is
	 * non-primitive.
	 * @param n The method call expression to examine
	 * @return true if at least one argument is non-primitive, false otherwise
	 */
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
	
	/**
	 * Determines if a type is primitive or a boxed primitive (e.g. Integer,
	 * Double, etc.). String is not considered primitive here.
	 * @param t The type to examine
	 * @return true if the type is primitive or a boxed primitive, false
	 * otherwise
	 */
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
	
	/**
	 * A token representing an assertion found by this finder.
	 */
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
