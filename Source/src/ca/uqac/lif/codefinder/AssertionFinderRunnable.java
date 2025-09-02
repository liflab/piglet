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
package ca.uqac.lif.codefinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import ca.uqac.lif.codefinder.assertion.AssertionFinder;
import ca.uqac.lif.codefinder.assertion.FoundToken;
import ca.uqac.lif.codefinder.util.StatusCallback;

/**
 * A runnable that processes a single Java file to find assertions.
 */
public class AssertionFinderRunnable implements Runnable
{
	/** The Java parser to use */
	protected final JavaParser m_parser;
	
	/** The file name */
	protected final String m_file;
	
	/** The contents of the file */
	protected final String m_is;
	
	/** The set of finders to use */
	protected final Set<AssertionFinder> m_finders;
	
	/** The set of found tokens */
	protected final Set<FoundToken> m_found;
	
	/** Whether to suppress warnings */
	protected final boolean m_quiet;
	
	/** A callback to report status */
	protected final StatusCallback m_callback;
	
	/**
	 * Creates a new runnable.
	 * @param p The Java parser to use
	 * @param file The file name
	 * @param code The contents of the file
	 * @param finders The set of finders to use
	 * @param found The set of found tokens
	 * @param quiet Whether to suppress warnings
	 * @param status A callback to report status
	 */
	public AssertionFinderRunnable(JavaParser p, String file, String code, Set<AssertionFinder> finders, boolean quiet, StatusCallback status)
	{
		super();
		m_parser = p;
		m_file = file;
		m_is = code;
		m_finders = finders;
		m_quiet = quiet;
		m_callback = status;
		m_found = new HashSet<FoundToken>();
	}
	
	@Override
	public void run()
	{
		processFile(m_parser, m_file, m_is, m_finders, m_quiet);
		if (m_callback != null)
		{
			m_callback.done();
		}
	}
	
	/**
	 * Processes a single Java file to find assertions.
	 * @param p The Java parser to use
	 * @param file The file name
	 * @param code The contents of the file
	 * @param finders The set of finders to use
	 * @param found The set of found tokens
	 * @param quiet Whether to suppress warnings
	 */
	protected void processFile(JavaParser p, String file, String code, Set<AssertionFinder> finders, boolean quiet)
	{
		try
		{
			CompilationUnit u = p.parse(code).getResult().get();
			List<MethodDeclaration> methods = getTestCases(u);
			/*if (methods.isEmpty() && !quiet)
			{
				// No test cases in this file
				System.err.println("WARNING: No test cases found in " + file);
			}*/
			for (MethodDeclaration m : methods)
			{
				for (AssertionFinder f : finders)
				{
					AssertionFinder new_f = f.newFinder(file);
					new_f.visit(m, null);
				}
			}
		}
		catch (NoSuchElementException e)
		{
			// Ignore this file
			if (!quiet)
			{
				System.err.println("Could not parse " + file);
			}
		}
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
}
