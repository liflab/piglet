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
package ca.uqac.lif.codefinder.find.sparql;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import ca.uqac.lif.codefinder.Main;
import ca.uqac.lif.codefinder.find.FoundToken;
import ca.uqac.lif.codefinder.find.ast.PushPopVisitableNode;
import ca.uqac.lif.codefinder.provider.FileSource;
import ca.uqac.lif.codefinder.thread.ThreadContext;
import ca.uqac.lif.codefinder.util.StatusCallback;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.FileUtils;

/**
 * A runnable that processes a single Java file to find assertions.
 */
public class SparqlAssertionFinderRunnable implements Runnable
{
	/** The file name */
	protected final String m_file;
	
	/** The file source from which to read */
	protected final FileSource m_fSource;
	
	/** The set of finders to use */
	protected final Set<SparqlTokenFinder> m_finders;
	
	/** The set of found tokens */
	protected final Set<FoundToken> m_found;
	
	/** Whether to suppress warnings */
	protected final boolean m_quiet;
	
	/** A callback to report status */
	protected final StatusCallback m_callback;
	
	/** The SPARQL query to execute */
	protected final String m_query;
	
	/**
	 * Creates a new runnable.
	 * @param context The thread context
	 * @param source The file source from which to read
	 * @param finders The set of finders to use
	 * @param found The set of found tokens
	 * @param quiet Whether to suppress warnings
	 * @param status A callback to report status
	 */
	public SparqlAssertionFinderRunnable(FileSource source, Set<SparqlTokenFinder> finders, boolean quiet, StatusCallback status, String query)
	{
		super();
		m_file = source.getFilename();
		m_fSource = source;
		m_finders = finders;
		m_quiet = quiet;
		m_callback = status;
		m_found = new HashSet<FoundToken>();
		m_query = query;
	}
	
	@Override
	public void run()
	{
		ThreadContext context = Main.CTX.get();
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
		processFile(context, m_file, code, m_finders, m_quiet);
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
	protected void processFile(ThreadContext context, String file, String code, Set<SparqlTokenFinder> finders, boolean quiet)
	{
		try
		{
			CompilationUnit u = context.getParser().parse(code).getResult().get();
			PushPopVisitableNode pm = new PushPopVisitableNode(u);
			ModelBuilder.ModelBuilderResult r = ModelBuilder.buildModel(pm);	    
			JavaAstNodeIndex globalAstIndex = r.getIndex();
			
			// Register property function
			PropertyFunctionRegistry.get()
		  .put("lif" + "resolvedType",
		       (uri) -> new ResolveType(globalAstIndex));
			
			ResultSet resultSet1 = QueryExecution.model(r.getModel())
					.query(JenaTest.prefixes + m_query).select();
			// TODO: parse result
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
