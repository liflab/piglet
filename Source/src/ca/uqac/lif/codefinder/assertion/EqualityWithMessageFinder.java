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
import com.github.javaparser.resolution.types.ResolvedType;

import ca.uqac.lif.codefinder.thread.ThreadContext;
import ca.uqac.lif.codefinder.util.Types;


/**
 * Finds assertions containing a call to method Object.equals.
 */
public class EqualityWithMessageFinder extends AssertionFinder
{	
	public EqualityWithMessageFinder(String filename)
	{
		super("With text message", filename);
	}
	
	protected EqualityWithMessageFinder(String filename, ThreadContext context)
	{
		super("With text message", filename, context);
	}

	@Override
	public AssertionFinder newFinder(String filename, ThreadContext context)
	{
		return new EqualityWithMessageFinder(filename, context);
	}

	@Override
	public void visit(MethodCallExpr n, Void v)
	{
		try {
			super.visit(n, v);
			if (isAssertionEquals(n) && hasMessage(n))
			{
				addToken(n);
			}
		} catch (Throwable t) {
			m_errors.add(t);
		}
	}

	protected boolean hasMessage(MethodCallExpr n)
	{
		if (n.getArguments().size() != 3)
		{
			return false;
		}
		try
		{
			ResolvedType type1 = Types.typeOf(n.getArgument(0), m_context.getTypeSolver());
			return type1.describe().compareTo("java.lang.String") == 0;
		}
		catch (Exception e)
		{
			// Unable to resolve type
			// Ignore for the moment
			return false;
		}
	}
}