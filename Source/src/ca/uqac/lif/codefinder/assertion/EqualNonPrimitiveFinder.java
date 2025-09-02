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

import java.util.Set;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.types.ResolvedType;

import ca.uqac.lif.codefinder.util.TypeChecks;
import ca.uqac.lif.codefinder.util.Types;

/**
 * Finds assertions that compare non-primitive values using equality.
 */
public class EqualNonPrimitiveFinder extends AssertionFinder
{
	protected static TypeSolver m_typeSolver = null;

	protected final Set<String> m_unresolved;

	public EqualNonPrimitiveFinder(String filename, TypeSolver ts, Set<String> unresolved)
	{
		super("Equality between non-primitive values", filename);
		m_typeSolver = ts;
		m_unresolved = unresolved;
	}

	@Override
	public AssertionFinder newFinder(String filename)
	{
		return new EqualNonPrimitiveFinder(filename, m_typeSolver, m_unresolved);
	}

	@Override
	public void visit(MethodCallExpr n, Set<FoundToken> set)
	{
		super.visit(n, set);
		try
		{
			if (isAssertionEquals(n) && hasNonPrimitive(n))
			{
				ResolvedType type1 = Types.typeOf(n.getArgument(0), m_typeSolver);
				if (TypeChecks.isSubtypeOf(type1, "java.util.Map"))
				{
					set.add(new EqualMapToken(m_filename, n.getBegin().get().line, n.toString()));
				}
				else if (TypeChecks.isSubtypeOf(type1, "java.util.List"))
				{
					set.add(new EqualListToken(m_filename, n.getBegin().get().line, n.toString()));
				}
				else if (TypeChecks.isSubtypeOf(type1, "java.util.Set"))
				{
					set.add(new EqualSetToken(m_filename, n.getBegin().get().line, n.toString()));
				}
				else if (type1.getClass().isArray())
				{
					set.add(new EqualArrayToken(m_filename, n.getBegin().get().line, n.toString()));
				}
				else
				{
					set.add(new EqualOtherNonPrimitiveToken(m_filename, n.getBegin().get().line, n.toString()));
				}
			}
		}
		catch (UnresolvedException e)
		{
			set.add(new EqualUnresolvedToken(m_filename, n.getBegin().get().line, n.toString()));
		}
	}

	/**
	 * Determines if at least one of the two arguments of the method call is
	 * non-primitive.
	 * @param n The method call expression to examine
	 * @return true if at least one argument is non-primitive, false otherwise
	 * @throws UnresolvedException 
	 */
	protected boolean hasNonPrimitive(MethodCallExpr n) throws UnresolvedException
	{
		if (n.getArguments().size() < 2)
		{
			return false;
		}
		boolean primitive1 = true;
		boolean primitive2 = true;
		boolean unresolved = false;
		try
		{
			ResolvedType type1 = Types.typeOf(n.getArgument(0), m_typeSolver);
			primitive1 = isPrimitive(type1);
		}
		catch (Exception e)
		{
			m_unresolved.add(n.getArgument(1).toString());
			unresolved = true;
		}
		try
		{
			ResolvedType type2 = Types.typeOf(n.getArgument(1), m_typeSolver);
			primitive2 = isPrimitive(type2);
		}
		catch (Exception e)
		{
			m_unresolved.add(n.getArgument(1).toString());
			unresolved = true;
		}
		if (unresolved)
		{
			throw new UnresolvedException();
		}
		return !primitive1 && !primitive2;
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
			super(filename, line, line, snippet);
		}

		@Override
		public String getAssertionName()
		{
			return "Equality between other objects";
		}
	}

	/**
	 * A token representing an assertion found by this finder.
	 */
	public class EqualMapToken extends FoundToken
	{
		public EqualMapToken(String filename, int line, String snippet)
		{
			super(filename, line, line, snippet);
		}

		@Override
		public String getAssertionName()
		{
			return "Equality between maps";
		}
	}

	/**
	 * A token representing an assertion found by this finder.
	 */
	public class EqualSetToken extends FoundToken
	{
		public EqualSetToken(String filename, int line, String snippet)
		{
			super(filename, line, line, snippet);
		}

		@Override
		public String getAssertionName()
		{
			return "Equality between maps";
		}
	}

	/**
	 * A token representing an assertion found by this finder.
	 */
	public class EqualListToken extends FoundToken
	{
		public EqualListToken(String filename, int line, String snippet)
		{
			super(filename, line, line, snippet);
		}

		@Override
		public String getAssertionName()
		{
			return "Equality between lists";
		}
	}

	/**
	 * A token representing an assertion found by this finder.
	 */
	public class EqualArrayToken extends FoundToken
	{
		public EqualArrayToken(String filename, int line, String snippet)
		{
			super(filename, line, line, snippet);
		}

		@Override
		public String getAssertionName()
		{
			return "Equality between arrays";
		}
	}

	/**
	 * A token representing an assertion found by this finder.
	 */
	public class EqualUnresolvedToken extends FoundToken
	{
		public EqualUnresolvedToken(String filename, int line, String snippet)
		{
			super(filename, line, line, snippet);
		}

		@Override
		public String getAssertionName()
		{
			return "Equality between unresolved symbols";
		}
	}

	protected static class UnresolvedException extends Throwable
	{
		/** Dummy UID **/
		private static final long serialVersionUID = 1L;		
	}

}
