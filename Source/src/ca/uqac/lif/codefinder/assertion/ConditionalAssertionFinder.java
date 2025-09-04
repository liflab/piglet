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
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;

import ca.uqac.lif.codefinder.thread.ThreadContext;


/**
 * Finds assertions nested inside an <tt>if</tt> block.
 */
public class ConditionalAssertionFinder extends AssertionFinder
{	
	protected int m_inCondition = 0;

	/**
	 * Creates a new conditional assertion finder.
	 * @param filename The name of the file to analyze
	 */
	public ConditionalAssertionFinder(String filename)
	{
		super("Conditional assertions", filename);
	}

	protected ConditionalAssertionFinder(String filename, ThreadContext context)
	{
		super("Conditional assertions", filename, context);
	}

	@Override
	public AssertionFinder newFinder(String filename, ThreadContext context)
	{
		return new ConditionalAssertionFinder(filename, context);
	}

	@Override
	public boolean visit(IfStmt n)
	{
		super.visit(n);
		m_inCondition++;
		return true;
	}
	
	@Override
	public boolean leave(IfStmt n)
	{
		super.leave(n);
		m_inCondition--;
		return true;
	}
	
	@Override
	public boolean visit(SwitchStmt n)
	{
		super.visit(n);
		m_inCondition++;
		return true;
	}
	
	@Override
	public boolean leave(SwitchStmt n)
	{
		super.leave(n);
		m_inCondition--;
		return true;
	}
	
	@Override
	public boolean visit(MethodCallExpr n)
	{
		super.visit(n);
		if (m_inCondition > 0 && isAssertion(n))
		{
			addToken(n);
			return false;
		}
		return true;
	}
}