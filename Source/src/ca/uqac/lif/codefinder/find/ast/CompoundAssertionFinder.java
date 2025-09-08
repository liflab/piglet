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

import java.util.List;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;

import ca.uqac.lif.codefinder.find.TokenFinderContext;
import ca.uqac.lif.codefinder.find.TokenFinderFactory;

/**
 * Finds assertions containing Boolean connectives.
 */
public class CompoundAssertionFinder extends AstAssertionFinder
{
	protected boolean m_inAssert;
	
	public CompoundAssertionFinder()
	{
		super("Compound assertions");
	}
	
	protected CompoundAssertionFinder(String name, String filename, TokenFinderContext context)
	{
		super(name, context);
	}
	
	@Override
	public boolean visit(MethodCallExpr n)
	{
		super.visit(n);
		if (!isAssertion(n))
		{
			return false;
		}
		try
		{
			if (isAssertionNotEquals(n) && containsCompound(n))
			{
				addToken(n);
				return false;
			}
		}
		catch (Throwable t)
		{
			m_errors.add(t);
		}
		return true;
	}

	protected static boolean containsCompound(Node n)
	{
		List<Node> children = n.getChildNodes();
		Node to_examine = children.get(1);
		if (children.size() == 3)
		{
			to_examine = children.get(2);
		}
		if (to_examine instanceof BinaryExpr)
		{
			BinaryExpr be = (BinaryExpr) to_examine;
			if (isComplex(be))
			{
				return true;
			}
		}
		return false;
	}

	protected static boolean isComplex(BinaryExpr be)
	{
		com.github.javaparser.ast.expr.BinaryExpr.Operator op = be.getOperator();
		switch (op)
		{
		case AND:
		case BINARY_AND:
		case BINARY_OR:
		//case DIVIDE:
		//case LEFT_SHIFT:
		//case MINUS:
		//case MULTIPLY:
		case OR:
		//case PLUS:
		//case REMAINDER:
		//case SIGNED_RIGHT_SHIFT:
		//case UNSIGNED_RIGHT_SHIFT:
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
	
	public static class CompoundAssertionFinderFactory extends TokenFinderFactory
	{
		@Override
		public CompoundAssertionFinder newFinder()
		{
			return new CompoundAssertionFinder();
		}
	}
}