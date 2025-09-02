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
 * Counts assertions, but does not store them.
 */
public abstract class AssertionCounter extends AssertionFinder
{	
	/** The count of found assertions */
	protected int m_count;
	
	public AssertionCounter(String name, String filename)
	{
		super(name, filename);
		m_count = 0;
	}

	@Override
	public void visit(MethodCallExpr n, Set<FoundToken> set)
	{
		super.visit(n, set);
		if (isNonFluentAssertion(n))
		{
			m_count++;
		}
	}
	
	@Override
	public int getFoundCount()
	{
		return m_count;
	}
}
