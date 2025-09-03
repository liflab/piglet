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

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

import ca.uqac.lif.codefinder.thread.ThreadContext;

/**
 * Finds assertions nested inside a for, while or do block.
 */
public class IteratedAssertionFinder extends AssertionFinder
{	
	public IteratedAssertionFinder(String filename)
	{
		super("Iterated assertions", filename);
	}
	
	protected IteratedAssertionFinder(String filename, ThreadContext context)
	{
		super("Iterated assertions", filename, context);
	}
	
	@Override
	public AssertionFinder newFinder(String filename, ThreadContext context)
	{
		return new IteratedAssertionFinder(filename, context);
	}
	
	@Override
	public void visit(ForStmt n, Void v)
	{
		try {
			super.visit(n, v);
			findAssertions(n, n, v);
		} catch (Throwable t) {
			m_errors.add(t);
		}
	}
	
	@Override
	public void visit(DoStmt n, Void v)
	{
		try {
			super.visit(n, v);
			findAssertions(n, n, v);
		} catch (Throwable t) {
			m_errors.add(t);
		}
	}
	
	@Override
	public void visit(WhileStmt n, Void v)
	{
		try {
			super.visit(n, v);
			findAssertions(n, n, v);
		} catch (Throwable t) {
			m_errors.add(t);
		}
	}
	
	public class IteratedAssertionToken extends FoundToken
	{
		public IteratedAssertionToken(String filename, int start_line, int end_line, String snippet)
		{
			super(IteratedAssertionFinder.this.getName(), filename, start_line, end_line, snippet);
		}	
	}
	
	protected void findAssertions(Node source, Node n, Void v)
	{
		if (n instanceof MethodCallExpr /* && isNonFluentAssertion((MethodCallExpr) n)*/)
		{
			int start = source.getBegin().get().line;
			int end = n.getBegin().get().line;
			m_found.add(new IteratedAssertionToken(m_filename, start, end, AssertionFinder.trimLines(source.toString(), end - start + 1)));
		}
		for (Node child : n.getChildNodes())
		{
			findAssertions(source, child, v);
		}
	}
}