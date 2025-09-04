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

import java.util.HashSet;
import java.util.Set;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;

import ca.uqac.lif.codefinder.thread.ThreadContext;
import ca.uqac.lif.codefinder.util.TypeChecks;
import ca.uqac.lif.codefinder.util.Types;
import ca.uqac.lif.codefinder.util.Types.ResolveResult;

/**
 * Finds assertions that compare non-primitive values using equality.
 */
public class EqualNonPrimitiveFinder extends AssertionFinder
{
	protected final Set<String> m_unresolved;

	public EqualNonPrimitiveFinder(String filename)
	{
		super("Equality between non-primitive values", filename);
		m_unresolved = new HashSet<>();
	}

	protected EqualNonPrimitiveFinder(String filename, ThreadContext context)
	{
		super("Equality between non-primitive values", filename, context);
		m_unresolved = new HashSet<>();
	}

	@Override
	public AssertionFinder newFinder(String filename, ThreadContext context)
	{
		return new EqualNonPrimitiveFinder(filename, context);
	}

	@Override
	public boolean visit(MethodCallExpr n)
	{
		super.visit(n);
		if (!isAssertionEquals(n))
		{
			return true;
		}
		ResolveResult<ResolvedType> res1 = Types.typeOfWithTimeout(n.getArgument(0), m_context.getTypeSolver(), m_context.getResolutionTimeout());
		if (res1.reason == Types.ResolveReason.UNSOLVED)
		{
			m_found.add(new EqualUnresolvedToken(m_filename, n.getBegin().get().line, n.toString()));
			return true;
		}
		if (res1.reason == Types.ResolveReason.TIMEOUT)
		{
			// TODO: report timeout
			return true;
		}
		ResolveResult<ResolvedType> res2 = Types.typeOfWithTimeout(n.getArgument(0), m_context.getTypeSolver(), m_context.getResolutionTimeout());
		if (res2.reason == Types.ResolveReason.UNSOLVED)
		{
			m_found.add(new EqualUnresolvedToken(m_filename, n.getBegin().get().line, n.toString()));
			return true;
		}
		if (res2.reason == Types.ResolveReason.TIMEOUT)
		{
			// TODO: report timeout
			return true;
		}
		if (hasNonPrimitive(res1.value.orElse(null), res2.value.orElse(null)))
		{
			if (res1.reason == Types.ResolveReason.RESOLVED)
			{
				ResolvedType type1 = res1.value.orElse(null);
				if (TypeChecks.isSubtypeOf(type1, "java.util.Map"))
				{
					m_found.add(new EqualMapToken(m_filename, n.getBegin().get().line, n.toString()));
				}
				else if (TypeChecks.isSubtypeOf(type1, "java.util.List"))
				{
					m_found.add(new EqualListToken(m_filename, n.getBegin().get().line, n.toString()));
				}
				else if (TypeChecks.isSubtypeOf(type1, "java.util.Set"))
				{
					m_found.add(new EqualSetToken(m_filename, n.getBegin().get().line, n.toString()));
				}
				else if (type1.getClass().isArray())
				{
					m_found.add(new EqualArrayToken(m_filename, n.getBegin().get().line, n.toString()));
				}
				else
				{
					m_found.add(new EqualOtherNonPrimitiveToken(m_filename, n.getBegin().get().line, n.toString()));
				}
			}
		}
		return true;
	}

	/**
	 * Determines if at least one of the two arguments of the method call is
	 * non-primitive.
	 * @param n The method call expression to examine
	 * @return true if at least one argument is non-primitive, false otherwise
	 */
	protected boolean hasNonPrimitive(ResolvedType type1, ResolvedType type2)
	{
		return !isPrimitive(type1) && !isPrimitive(type2);
	}

	/**
	 * Determines if a type is primitive or a boxed primitive (e.g. Integer,
	 * Double, etc.).
	 * @param t The type to examine
	 * @return true if the type is primitive or a boxed primitive, false
	 * otherwise
	 */
	protected static boolean isPrimitive(ResolvedType t)
	{
		if (t == null)
		{
			return false;
		}
		if (t.isPrimitive())
		{
			return true;
		}
		String type_name = t.describe();
		if (type_name.equals("java.lang.String") || type_name.equals("java.lang.Integer") || type_name.equals("java.lang.Long")
				|| type_name.equals("java.lang.Float") || type_name.equals("java.lang.Double") || type_name.equals("java.lang.Byte")
				|| type_name.equals("java.lang.Short") || type_name.equals("java.lang.Character") || type_name.equals("java.lang.Boolean"))
		{
			return true;
		}
		return false;
	}

	protected static String getTypeName(ResolvedType t)
	{
		return t.describe();
	}

	/**
	 * A token representing an assertion found by this finder.
	 */
	public class EqualOtherNonPrimitiveToken extends FoundToken
	{
		public EqualOtherNonPrimitiveToken(String filename, int line, String snippet)
		{
			super("Equality between other objects", filename, line, line, snippet);
		}
	}

	/**
	 * A token representing an assertion found by this finder.
	 */
	public class EqualMapToken extends FoundToken
	{
		public EqualMapToken(String filename, int line, String snippet)
		{
			super("Equality between maps", filename, line, line, snippet);
		}
	}

	/**
	 * A token representing an assertion found by this finder.
	 */
	public class EqualSetToken extends FoundToken
	{
		public EqualSetToken(String filename, int line, String snippet)
		{
			super("Equality between maps", filename, line, line, snippet);
		}
	}

	/**
	 * A token representing an assertion found by this finder.
	 */
	public class EqualListToken extends FoundToken
	{
		public EqualListToken(String filename, int line, String snippet)
		{
			super("Equality between lists", filename, line, line, snippet);
		}
	}

	/**
	 * A token representing an assertion found by this finder.
	 */
	public class EqualArrayToken extends FoundToken
	{
		public EqualArrayToken(String filename, int line, String snippet)
		{
			super("Equality between arrays", filename, line, line, snippet);
		}
	}

	/**
	 * A token representing an assertion found by this finder.
	 */
	public class EqualUnresolvedToken extends FoundToken
	{
		public EqualUnresolvedToken(String filename, int line, String snippet)
		{
			super("Equality between unresolved symbols", filename, line, line, snippet);
		}
	}

	protected static class UnresolvedException extends Throwable
	{
		/** Dummy UID **/
		private static final long serialVersionUID = 1L;	
		
		public UnresolvedException(String message)
		{
			super(message);
		}
	}

}