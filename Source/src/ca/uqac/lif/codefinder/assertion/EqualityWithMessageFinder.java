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

import ca.uqac.lif.codefinder.util.Types;


/**
 * Finds assertions containing a call to method Object.equals.
 */
public class EqualityWithMessageFinder extends AssertionFinder
{	
	protected final TypeSolver m_typeSolver;
	
	public EqualityWithMessageFinder(String filename, TypeSolver ts)
	{
		super("With text message", filename);
		m_typeSolver = ts;
	}

	@Override
	public AssertionFinder newFinder(String filename)
	{
		return new EqualityWithMessageFinder(filename, m_typeSolver);
	}

	@Override
	public void visit(MethodCallExpr n, Set<FoundToken> set)
	{
		super.visit(n, set);
		if (isAssertionEquals(n) && hasMessage(n))
		{
			set.add(new EqualityWithMessageToken(m_filename, n.getBegin().get().line, n.toString()));
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
			ResolvedType type1 = Types.strictTypeOfOrNullIfSkipped(n.getArgument(0), m_typeSolver);
			return type1.describe().compareTo("java.lang.String") == 0;
		}
		catch (Exception e)
		{
			// Unable to resolve type
			// Ignore for the moment
			return false;
		}
	}

	public class EqualityWithMessageToken extends FoundToken
	{
		public EqualityWithMessageToken(String filename, int line, String snippet)
		{
			super(filename, line, line, snippet);
		}		

		@Override
		public String getAssertionName()
		{
			return EqualityWithMessageFinder.this.getName();
		}
	}
}
