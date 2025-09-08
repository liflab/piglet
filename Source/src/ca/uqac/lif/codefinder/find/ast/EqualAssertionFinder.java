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
package ca.uqac.lif.codefinder.find.ast;

import com.github.javaparser.ast.expr.MethodCallExpr;

import ca.uqac.lif.codefinder.find.TokenFinderContext;
import ca.uqac.lif.codefinder.find.TokenFinderFactory;


/**
 * Finds assertions containing a call to method Object.equals.
 */
public class EqualAssertionFinder extends AstAssertionFinder
{	
	protected boolean m_inAssert = false;

	protected boolean m_foundEquals = false;

	public EqualAssertionFinder()
	{
		this(null);
	}

	protected EqualAssertionFinder(TokenFinderContext context)
	{
		super("Object.equals", context);
	}

	@Override
	public boolean visit(MethodCallExpr n)
	{
		super.visit(n);
		if (!isAssertion(n))
		{
			if (m_inAssert && isEquals(n))
			{
				m_foundEquals = true;
			}
			return false;
		}
		m_inAssert = true;
		return true;
	}

	@Override
	public boolean leave(MethodCallExpr n)
	{
		super.leave(n);
		if (isAssertion(n))
		{
			m_inAssert = false;
			if (m_foundEquals)
			{
				addToken(n);
				m_foundEquals = false;
			}
		}
		return true;
	}

	protected static boolean isEquals(MethodCallExpr me)
	{
		return me.getName().asString().compareTo("equals") == 0;
	}

	public static class EqualAssertionFinderFactory extends TokenFinderFactory
	{
		@Override
		public EqualAssertionFinder newFinder()
		{
			return new EqualAssertionFinder();
		}
	}
}