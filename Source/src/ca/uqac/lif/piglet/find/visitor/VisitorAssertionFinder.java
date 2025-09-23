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
package ca.uqac.lif.piglet.find.visitor;

import com.github.javaparser.ast.expr.MethodCallExpr;

import ca.uqac.lif.piglet.find.TokenFinderContext;

/**
 * An abstract base class for finders that look for assertions in Java code.
 */
public abstract class VisitorAssertionFinder extends VisitorTokenFinder
{
	/**
	 * Creates a new assertion finder.
	 * @param name The name of this finder
	 * @param filename The name of the file to analyze
	 */
	public VisitorAssertionFinder(String name)
	{
		super(name);
	}
	
	/**
	 * Creates a new assertion finder.
	 * @param name The name of this finder
	 * @param parser A Java parser instance
	 */
	protected VisitorAssertionFinder(String name, TokenFinderContext context)
	{
		super(name, context);
		
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
	
}
