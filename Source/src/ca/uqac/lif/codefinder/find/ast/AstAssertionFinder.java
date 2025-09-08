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

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.github.javaparser.ast.expr.MethodCallExpr;

import ca.uqac.lif.codefinder.find.FoundToken;
import ca.uqac.lif.codefinder.find.TokenFinderContext;

/**
 * An abstract base class for finders that look for assertions in Java code.
 */
public abstract class AstAssertionFinder extends AstTokenFinder
{
	/** The set of found tokens */
	protected final Set<FoundToken> m_found;
	
	/**
	 * The set of errors found during parsing
	 */
	protected final Set<Throwable> m_errors = new HashSet<Throwable>();
	
	/**
	 * Creates a new assertion finder.
	 * @param name The name of this finder
	 * @param filename The name of the file to analyze
	 */
	public AstAssertionFinder(String name)
	{
		super(name);
		m_found = new TreeSet<FoundToken>();
	}
	
	/**
	 * Creates a new assertion finder.
	 * @param name The name of this finder
	 * @param parser A Java parser instance
	 */
	protected AstAssertionFinder(String name, TokenFinderContext context)
	{
		super(name, context);
		m_found = new TreeSet<FoundToken>();
	}
	
	/**
	 * Gets the set of found tokens.
	 * @return The set of found tokens
	 */
	@Override
	public Set<FoundToken> getFoundTokens()
	{
		return m_found;
	}
	
	@Override
	public int getFoundCount()
	{
		return m_found.size();
	}
	
	/**
	 * Adds a found token to the set of found tokens.
	 * @param n The method call expression corresponding to the found token
	 */
	public void addToken(MethodCallExpr n)
	{
		m_found.add(new FoundToken(m_name, m_filename, n.getBegin().get().line, n.getEnd().get().line, n.toString()));
	}
	
	/**
	 * Adds a found token to the set of found tokens.
	 * @param start The starting line number of the found token
	 * @param end The ending line number of the found token
	 * @param snippet A snippet of code corresponding to the found token
	 */
	protected void addToken(int start, int end, String snippet)
	{
		m_found.add(new FoundToken(m_name, m_filename, start, end, snippet));
	}
	
	/**
	 * Determines if a method call expression is an assertEquals call.
	 * @param m The method call expression to examine
	 * @return <tt>true</tt> if the method call is an assertEquals call, <tt>false</tt>
	 * otherwise
	 */
	protected static boolean isAssertionEquals(MethodCallExpr m)
	{
		String name = m.getName().asString();
		return name.compareTo("assertEquals") == 0;
	}
	
	/**
	 * Determines if a method call expression is an assertion, but that is not
	 * assertEquals.
	 * @param m The method call expression to examine
	 * @return <tt>true</tt> if the method call is an assertion other than
	 * assertEquals, <tt>false</tt> otherwise
	 */
	protected static boolean isAssertionNotEquals(MethodCallExpr m)
	{
		String name = m.getName().asString();
		return name.compareTo("assert")
				* name.compareTo("assertTrue")
				* name.compareTo("assertFalse") == 0;
	}
	
	/**
	 * Determines if a method call expression is an assertThat call.
	 * @param m The method call expression to examine
	 * @return <tt>true</tt> if the method call is an assertThat call, <tt>false</tt>
	 * otherwise
	 */
	protected static boolean isAssertThat(MethodCallExpr m)
	{
		String name = m.getName().asString();
		return name.compareTo("assertThat") == 0;
	}
	
	/**
	 * Determines if a method call expression is an assertion.
	 * @param m The method call expression to examine
	 * @return <tt>true</tt> if the method call is an assertion, <tt>false</tt>
	 * otherwise
	 */
	protected static boolean isAssertion(MethodCallExpr m)
	{
		return isAssertionEquals(m) || isAssertionNotEquals(m) || isAssertThat(m);
	}
	
	@Override
	public Set<Throwable> getErrors()
	{
		return m_errors;
	}
}
