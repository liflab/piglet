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

/**
 * Counts non-fluent assertions (that is, assertions not using
 * assertThat) but does not store them.
 */
public class NonFluentAssertionsCounter extends AssertionCounter
{
	/**
	 * Creates a new non-fluent assertions counter.
	 * @param filename
	 */
	public NonFluentAssertionsCounter()
	{
		this(null);
	}

	protected NonFluentAssertionsCounter(TokenFinderContext context)
	{
		super("Non-fluent assertions", context);
	}

	@Override
	public boolean visit(MethodCallExpr n)
	{
		super.visit(n);
		if (!isNonFluentAssertion(n))
		{
			return false;
		}
		addToken(n);
		return false;
	}
	
	/**
	 * Determines if a method call expression is an assertion, written
	 * in a non-fluent style (i.e. not using assertThat).
	 * @param m The method call expression to examine
	 * @return <tt>true</tt> if the method call is an assertion, <tt>false</tt>
	 * otherwise
	 */
	protected static boolean isNonFluentAssertion(MethodCallExpr m)
	{
		String name = m.getName().asString();
		return name.compareTo("assert")
				* name.compareTo("assertEquals")
				* name.compareTo("assertTrue")
				* name.compareTo("assertFalse") == 0;
	}
	
	public static class NonFluentAssertionsCounterFactory extends AstAssertionFinderFactory
	{
		@Override
		public NonFluentAssertionsCounter newFinder()
		{
			return new NonFluentAssertionsCounter();
		}
	}
}