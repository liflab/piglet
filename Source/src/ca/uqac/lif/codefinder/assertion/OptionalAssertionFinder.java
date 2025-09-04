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
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;

import ca.uqac.lif.codefinder.thread.ThreadContext;
import ca.uqac.lif.codefinder.util.TypeChecks;
import ca.uqac.lif.codefinder.util.Types;

/**
 * Finds assertions involving the use of optional values.
 */
public class OptionalAssertionFinder extends AssertionFinder
{
	/**
	 * Creates a new optional assertion finder.
	 * @param name The name of the finder
	 * @param filename The name of the file in which the finder is defined
	 */
	public OptionalAssertionFinder(String filename)
	{
		super("Assertions with Optional", filename);
	}
	
	/**
	 * Creates a new optional assertion finder.
	 * @param name The name of the finder
	 * @param filename The name of the file in which the finder is defined
	 * @param context The thread context in which the finder is executed
	 */
	protected OptionalAssertionFinder(String filename, ThreadContext context)
	{
		super("Assertions with Optional", filename, context);
	}

	@Override
	public OptionalAssertionFinder newFinder(String filename, ThreadContext context)
	{
		return new OptionalAssertionFinder(filename, context);
	}
	
	@Override
	public void visit(MethodCallExpr n, Void v)
	{
		try {
			super.visit(n, v);
			if (containsOptional(n))
			{
				addToken(n);
			}
		} catch (Throwable t) {
			m_errors.add(t);
		}
	}
	
	/**
	 * Determines if an expression contains an optional value.
	 * @param n The expression to examine
	 * @return <tt>true</tt> if the expression contains an optional value,
	 * <tt>false</tt> otherwise
	 */
	protected boolean containsOptional(Expression n)
	{
		ResolvedType type1 = Types.smartTypeOf(n, null, m_context.getTypeSolver(), m_context.getResolutionTimeout()).orElse(null);
		if (type1 == null)
		{
			return false;
		}
		if (TypeChecks.isOptionalType(type1))
		{
			return true;
		}
		for (Node c : n.getChildNodes())
		{
			if (c instanceof Expression)
			{
				Expression e = (Expression) c;
				if (containsOptional(e))
				{
					return true;
				}
			}
		}
		return false;
	}
}