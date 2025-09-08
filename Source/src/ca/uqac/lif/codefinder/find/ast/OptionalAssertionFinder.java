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

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;

import ca.uqac.lif.codefinder.find.TokenFinderContext;
import ca.uqac.lif.codefinder.util.TypeChecks;
import ca.uqac.lif.codefinder.util.Types;
import ca.uqac.lif.codefinder.util.Types.ResolveReason;
import ca.uqac.lif.codefinder.util.Types.ResolveResult;

/**
 * Finds assertions involving the use of optional values.
 */
public class OptionalAssertionFinder extends TypeAwareAssertionFinder
{
	protected boolean m_foundOptional = false;
	
	protected boolean m_inAssert = false;
	
	/**
	 * Creates a new optional assertion finder.
	 * @param name The name of the finder
	 * @param filename The name of the file in which the finder is defined
	 */
	public OptionalAssertionFinder()
	{
		this(null);
	}
	
	/**
	 * Creates a new optional assertion finder.
	 * @param name The name of the finder
	 * @param filename The name of the file in which the finder is defined
	 * @param context The thread context in which the finder is executed
	 */
	protected OptionalAssertionFinder(TokenFinderContext context)
	{
		super("Assertions with Optional", context);
	}
	
	@Override
	public boolean visit(MethodCallExpr n)
	{
		super.visit(n);
		if (!isAssertion(n))
		{
			return false;
		}
		m_inAssert = true;
		return true;
	}
	
	@Override
	public boolean leave(MethodCallExpr n)
	{
		super.leave(n);
		if (isAssertion(n) && m_foundOptional)
		{
			addToken(n);
			m_inAssert = false;
			m_foundOptional = false;
		}
		return true;
		
	}
	
	@Override
	public boolean visitTypedNode(Node n)
	{
		if (!(n instanceof Expression))
		{
			return true;
		}
		if (m_inAssert && isOptional((Expression) n))
		{
			m_foundOptional = true;
		}
		return false;
	}

	
	protected boolean isOptional(Expression n)
	{
		ResolveResult<ResolvedType> rr = Types.typeOfWithTimeout(n, m_context.getTypeSolver(), m_context.getResolutionTimeout());
		if (rr.reason == ResolveReason.UNSOLVED || rr.reason == ResolveReason.TIMEOUT)
		{
			return false;
		}
		ResolvedType type1 = rr.value.orElse(null);
		return TypeChecks.isOptionalType(type1);
	}	
	
	public static class OptionalAssertionFinderFactory extends AstAssertionFinderFactory
	{
		@Override
		public OptionalAssertionFinder newFinder()
		{
			return new OptionalAssertionFinder();
		}
	}
}