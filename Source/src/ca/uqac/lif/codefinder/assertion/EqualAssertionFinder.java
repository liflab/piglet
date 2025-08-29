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

import java.util.List;
import java.util.Set;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;


/**
 * Finds assertions containing a call to method Object.equals.
 */
public class EqualAssertionFinder extends AssertionFinder
{	
	public EqualAssertionFinder(String filename)
	{
		super("Object.equals", filename);
	}
	
	@Override
	public AssertionFinder newFinder(String filename)
	{
		return new EqualAssertionFinder(filename);
	}

	@Override
	public void visit(MethodCallExpr n, Set<FoundToken> set)
	{
		super.visit(n, set);
		if (isAssertionEquals(n) && containsEquals(n))
		{
			set.add(new EqualAssertionToken(m_filename, n.getBegin().get().line, n.toString()));
		}
	}

	protected static boolean containsEquals(Node n)
	{
		List<Node> children = n.getChildNodes();
		for (int i = 1; i < children.size(); i++)
		{
			Node child = children.get(i);
			if (child instanceof MethodCallExpr)
			{
				MethodCallExpr me = (MethodCallExpr) child;
				if (me.getName().asString().compareTo("equals") == 0)
				{
					return true;
				}
			}
			if (containsEquals(child))
			{
				return true;
			}
		}
		return false;
	}
	
	public class EqualAssertionToken extends FoundToken
	{
		public EqualAssertionToken(String filename, int line, String snippet)
		{
			super(filename, line, line, snippet);
		}		
		
		@Override
		public String getAssertionName()
		{
			return EqualAssertionFinder.this.getName();
		}
	}

	protected static boolean isComplex(BinaryExpr be)
	{
		com.github.javaparser.ast.expr.BinaryExpr.Operator op = be.getOperator();
		switch (op)
		{
		case AND:
		case BINARY_AND:
		case BINARY_OR:
		case DIVIDE:
		case LEFT_SHIFT:
		case MINUS:
		case MULTIPLY:
		case OR:
		case PLUS:
		case REMAINDER:
		case SIGNED_RIGHT_SHIFT:
		case UNSIGNED_RIGHT_SHIFT:
		case XOR:
			return true;
		case EQUALS:
		case GREATER:
		case GREATER_EQUALS:
		case LESS:
		case LESS_EQUALS:
		case NOT_EQUALS:
		default:
			return false;
		}

	}
}
