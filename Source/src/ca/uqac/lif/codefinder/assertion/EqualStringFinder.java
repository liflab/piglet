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

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;

import ca.uqac.lif.codefinder.thread.ThreadContext;

/**
 * Finds assertions stating the equality of two strings.
 */
public class EqualStringFinder extends AssertionFinder
{
	public EqualStringFinder(String filename)
	{
		super("Equality between strings", filename);
	}
	
	protected EqualStringFinder(String filename, ThreadContext context)
	{
		super("Equality between strings", filename, context);
	}
	
	@Override
	public AssertionFinder newFinder(String filename, ThreadContext context)
	{
		return new EqualStringFinder(filename, context);
	}
	
	@Override
	public void visit(MethodCallExpr n, Void v)
	{
		super.visit(n, v);
		if (isAssertionEquals(n) && comparesStrings(n))
		{
			addToken(n);
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

}
