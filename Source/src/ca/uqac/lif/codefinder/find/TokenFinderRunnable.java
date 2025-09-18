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
package ca.uqac.lif.codefinder.find;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import ca.uqac.lif.codefinder.provider.FileSource;
import ca.uqac.lif.codefinder.util.StatusCallback;

public abstract class TokenFinderRunnable implements Runnable
{
	/** The file name */
	protected final String m_file;
	
	/** The file source from which to read */
	protected final FileSource m_fSource;
	
	/** The set of found tokens */
	protected final Set<FoundToken> m_found;
	
	/** Whether to suppress warnings */
	protected final boolean m_quiet;
	
	/** A callback to report status */
	protected final StatusCallback m_callback;
	
	/** The set of finders to use */
	protected final Set<? extends TokenFinderFactory> m_finders;
	
	/**
	 * Creates a new runnable.
	 * @param source The file source from which to read
	 * @param found The set of found tokens
	 * @param quiet Whether to suppress warnings
	 * @param status A callback to report status
	 */
	public TokenFinderRunnable(String file, FileSource source, boolean quiet, StatusCallback status, Set<? extends TokenFinderFactory> finders)
	{
		super();
		m_file = file;
		m_fSource = source;
		m_found = new HashSet<FoundToken>();
		m_quiet = quiet;
		m_callback = status;
		m_finders = new HashSet<TokenFinderFactory>(finders);
	}

	public Set<FoundToken> getFound()
	{
		return m_found;
	}
	
	/**
	 * Gets the list of test cases in a compilation unit.
	 * @param u The compilation unit
	 * @return The list of test cases
	 */
	protected static List<MethodDeclaration> getTestCases(CompilationUnit u)
	{
		List<MethodDeclaration> list = new ArrayList<MethodDeclaration>();
		List<MethodDeclaration> methods = u.findAll(MethodDeclaration.class);
		for (MethodDeclaration m : methods)
		{
			if (isTest(m))
			{
				list.add(m);
			}
		}
		return list;
	}

	/**
	 * Determines whether a method is a test case.
	 * @param m The method
	 * @return <tt>true</tt> if the method is a test case, <tt>false</tt> otherwise
	 */
	protected static boolean isTest(MethodDeclaration m)
	{
		for (AnnotationExpr a : m.getAnnotations())
		{
			if (a.getName().asString().compareTo("Test") == 0)
			{
				return true;
			}
		}
		return false;
	}
	
	protected boolean mustRun()
	{
		
		return true;
	}
	

}
