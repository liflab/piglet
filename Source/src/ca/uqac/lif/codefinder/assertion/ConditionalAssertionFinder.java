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
import com.github.javaparser.ast.stmt.IfStmt;


/**
 * Finds assertions nested inside an <tt>if</tt> block.
 */
public class ConditionalAssertionFinder extends AssertionFinder
{	
	/**
	 * Creates a new conditional assertion finder.
	 * @param filename The name of the file to analyze
	 */
	public ConditionalAssertionFinder(String filename)
	{
		super("Conditional assertions", filename);
	}

	@Override
	public AssertionFinder newFinder(String filename)
	{
		return new ConditionalAssertionFinder(filename);
	}

	@Override
	public void visit(IfStmt n, Void v)
	{
		super.visit(n, v);
		findAssertions(n, n);

	}

	/**
	 * Recursively finds assertions in a node.
	 * @param source The source node to extract line numbers from
	 * @param n The node to examine
	 */
	protected void findAssertions(Node source, Node n)
	{
		if (n instanceof MethodCallExpr /*&& isNonFluentAssertion((MethodCallExpr) n)*/)
		{
			int start = source.getBegin().get().line;
			int end = n.getBegin().get().line;
			addToken(start, end, AssertionFinder.trimLines(source.toString(), end - start + 1));
		}
		for (Node child : n.getChildNodes())
		{
			findAssertions(source, child);
		}
	}
}
