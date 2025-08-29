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

/**
 * Finds all assertions in a Java file.
 */
public class AnyAssertionFinder extends AssertionFinder
{	
	public static final String NAME = "All assertions";
	
	public AnyAssertionFinder(String filename)
	{
		super(NAME, filename);
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
