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
import com.github.javaparser.resolution.types.ResolvedType;

import ca.uqac.lif.codefinder.find.TokenFinderContext;
import ca.uqac.lif.codefinder.find.ast.EqualNonPrimitiveFinder.UnresolvedException;
import ca.uqac.lif.codefinder.util.Types;
import ca.uqac.lif.codefinder.util.Types.ResolveReason;
import ca.uqac.lif.codefinder.util.Types.ResolveResult;

/**
 * Finds assertions stating the equality of two strings.
 */
public class EqualStringFinder extends AstAssertionFinder
{
	public EqualStringFinder()
	{
		this(null);
	}

	protected EqualStringFinder(TokenFinderContext context)
	{
		super("Equality between strings", context);
	}

	@Override
	public boolean visit(MethodCallExpr n)
	{
		super.visit(n);
		if (!isAssertionEquals(n))
		{
			return false;
		}
		try
		{
			if (comparesStrings(n))
			{
				addToken(n);
				return false;
			}
		}
		catch (UnresolvedException e)
		{
			m_errors.add(e);
		}
		return true;
	}

	/**
	 * Determines whether the given method call expression compares two strings.
	 * @param n The method call expression to examine
	 * @return true if the method call compares two strings, false otherwise
	 * @throws UnresolvedException 
	 */
	protected boolean comparesStrings(MethodCallExpr n) throws UnresolvedException
	{
		if (n.getArguments().size() < 2)
		{
			return false;
		}

		ResolveResult<ResolvedType> rr1 = Types.typeOfWithTimeout(n.getArgument(0), m_context.getTypeSolver(), m_context.getResolutionTimeout());
		ResolveResult<ResolvedType> rr2 = Types.typeOfWithTimeout(n.getArgument(0), m_context.getTypeSolver(), m_context.getResolutionTimeout());
		if ((rr1.reason == ResolveReason.RESOLVED && rr1.value.orElse(null).describe().equals("java.lang.String")) ||
				(rr2.reason == ResolveReason.RESOLVED && rr2.value.orElse(null).describe().equals("java.lang.String")))
		{
			return true;
		}
		if (rr1.reason == ResolveReason.TIMEOUT || rr2.reason == ResolveReason.TIMEOUT)
		{
			throw new UnresolvedException("Timeout while resolving type in " + n.toString());
		}
		return false;
	}
	
	public static class EqualStringFinderFactory extends AstAssertionFinderFactory
	{
		@Override
		public EqualStringFinder newFinder()
		{
			return new EqualStringFinder();
		}
	}

}