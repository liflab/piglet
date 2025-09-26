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
package ca.uqac.lif.piglet.find;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.FileUtils;
import ca.uqac.lif.piglet.Main;
import ca.uqac.lif.piglet.find.TokenFinder.TokenFinderException;
import ca.uqac.lif.piglet.provider.FileSource;
import ca.uqac.lif.piglet.util.StatusCallback;

/**
 * A callable object that runs a set of token finders on a given file.
 */
public abstract class TokenFinderCallable implements Callable<TokenFinderCallable.CallableFuture>
{
	/** The file name */
	protected final String m_file;
	
	/** The project name */
	protected final String m_project;
	
	/** The file source from which to read */
	protected final FileSource m_fSource;
	
	/** Whether to suppress warnings */
	protected final boolean m_quiet;
	
	/** A callback to report status */
	protected final StatusCallback m_callback;
	
	/** The set of finders to use */
	protected final Set<? extends TokenFinderFactory> m_finders;
	
	/**
	 * Creates a new callable.
	 * @param project The project name
	 * @param file The file name
	 * @param source The file source from which to read
	 * @param found The set of found tokens
	 * @param quiet Whether to suppress warnings
	 * @param status A callback to report status
	 */
	public TokenFinderCallable(String project, String file, FileSource source, boolean quiet, StatusCallback status, Set<? extends TokenFinderFactory> finders)
	{
		super();
		m_project = project;
		m_file = file;
		m_fSource = source;
		m_quiet = quiet;
		m_callback = status;
		m_finders = new HashSet<TokenFinderFactory>(finders);
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
			//if (isTest(m))
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
	
	@Override
	public final CallableFuture call()
	{
		if (m_callback != null)
		{
			m_callback.setCurrentProject(m_project);
		}
		TokenFinderContext context = Main.CTX.get();
		InputStream is;
		String code = "";
		try
		{
			is = m_fSource.getStream();
			code = new String(FileUtils.toBytes(is));
			is.close();
		}
		catch (FileSystemException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Set<FoundToken> found = new HashSet<FoundToken>();
		try
		{
			doRun(context, code, found);
		}
		catch (TokenFinderException e)
		{
			if (m_callback != null)
			{
				m_callback.error("Error processing " + m_file + ": " + e.getMessage());
			}
			return new CallableFuture(m_file, found);
		}
		if (m_callback != null)
		{
			m_callback.done();
		}
		return new CallableFuture(m_file, found);
	}
	
	/**
	 * Gets the file name.
	 * @return The file name
	 */
	public String getFileName()
	{
		return m_file;
	}
	
	protected abstract void doRun(TokenFinderContext context, String code, Set<FoundToken> found) throws TokenFinderException;
	
	/**
	 * The result of a callable.
	 */
	public static class CallableFuture
	{
		/** The file name */
		protected final String m_filename;
		
		/** The set of found tokens */
		protected final Set<FoundToken> m_found;
		
		/**
		 * Creates a new callable future.
		 * @param filename The file name
		 * @param found The set of found tokens
		 */
		CallableFuture(String filename, Set<FoundToken> found) 
		{
			super();
			m_filename = filename;
			m_found = found;
		}
		
		/**
		 * Gets the file name.
		 * @return The file name
		 */
		public String getFileName()
		{
			return m_filename;
		}
		
		/**
		 * Gets the set of found tokens.
		 * @return The set of found tokens
		 */
		public Set<FoundToken> getFoundTokens()
		{
			return m_found;
		}
	}

}
