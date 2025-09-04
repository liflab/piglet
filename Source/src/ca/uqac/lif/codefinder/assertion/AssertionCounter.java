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

import ca.uqac.lif.codefinder.thread.ThreadContext;

/**
 * Counts fluent assertions (i.e., using assertThat)
 * but does not store them.
 */
public abstract class AssertionCounter extends AssertionFinder
{	
	/** The count of found assertions */
	protected int m_count;
	
	public AssertionCounter(String name, String filename)
	{
		super(name + " count", filename);
		m_count = 0;
	}
	
	protected AssertionCounter(String name, String filename, ThreadContext context)
	{
		super(name + " count", filename, context);
		m_count = 0;
	}

	@Override
	public boolean visit(MethodCallExpr n)
	{
		super.visit(n);
		if (isFluentAssertion(n))
		{
			m_count++;
			return false;
		}
		return true;
	}
	
	@Override
	public int getFoundCount()
	{
		return m_count;
	}
	
	/**
	 * Determines if a method call expression is a fluent assertion,
	 * i.e. using assertThat.
	 * @param m The method call expression to examine
	 * @return <tt>true</tt> if the method call is a fluent assertion, <tt>false</tt>
	 * otherwise
	 */
	protected static boolean isFluentAssertion(MethodCallExpr m)
	{
		String name = m.getName().asString();
		return name.compareTo("assertThat") == 0;
	}
}