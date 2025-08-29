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
 * Finds assertions stating the equality of two strings.
 */
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
