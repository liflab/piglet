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
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

import ca.uqac.lif.codefinder.find.FoundToken;
import ca.uqac.lif.codefinder.find.TokenFinderContext;
import ca.uqac.lif.codefinder.find.TokenFinderFactory;

/**
 * Finds assertions nested inside a for, while or do block.
 */
public class IteratedAssertionFinder extends AstAssertionFinder
{
	protected int m_inLoop = 0;
	
	public IteratedAssertionFinder()
	{
		super(null);
	}
	
	protected IteratedAssertionFinder(TokenFinderContext context)
	{
		super("Iterated assertions", context);
	}
	
	@Override
	public boolean visit(ForStmt n)
	{
		super.visit(n);
		m_inLoop++;
		return true;
	}
	
	@Override
	public boolean leave(ForStmt n)
	{
		super.leave(n);
		m_inLoop--;
		return true;
	}
	
	@Override
	public boolean visit(DoStmt n)
	{
		super.visit(n);
		m_inLoop++;
		return true;
	}
	
	@Override
	public boolean leave(DoStmt n)
	{
		super.leave(n);
		m_inLoop--;
		return true;
	}
	
	@Override
	public boolean visit(WhileStmt n)
	{
		super.visit(n);
		m_inLoop++;
		return true;
	}
	
	@Override
	public boolean leave(WhileStmt n)
	{
		super.leave(n);
		m_inLoop--;
		return true;
	}
	
	@Override
	public boolean visit(MethodCallExpr n)
	{
		super.visit(n);
		if (m_inLoop > 0 && isAssertion(n))
		{
			addToken(n);
			return false;
		}
		return true;
	}
	
	public class IteratedAssertionToken extends FoundToken
	{
		public IteratedAssertionToken(String filename, int start_line, int end_line, String snippet)
		{
			super(IteratedAssertionFinder.this.getName(), filename, start_line, end_line, snippet);
		}	
	}
	
	public static class IteratedAssertionFinderFactory extends TokenFinderFactory
	{
		@Override
		public IteratedAssertionFinder newFinder()
		{
			return new IteratedAssertionFinder();
		}
	}
}