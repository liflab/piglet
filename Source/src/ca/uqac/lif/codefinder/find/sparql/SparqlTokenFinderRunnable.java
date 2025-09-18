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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;

import ca.uqac.lif.codefinder.find.FoundToken;
import ca.uqac.lif.codefinder.find.TokenFinderContext;
import ca.uqac.lif.codefinder.find.TokenFinderFactory;
import ca.uqac.lif.codefinder.find.TokenFinderRunnable;
import ca.uqac.lif.codefinder.find.visitor.PushPopVisitableNode;
import ca.uqac.lif.codefinder.provider.FileSource;
import ca.uqac.lif.codefinder.util.StatusCallback;

/**
 * A runnable that processes a single Java file to find assertions.
 */
public class SparqlTokenFinderRunnable extends TokenFinderRunnable
{	
	/** Whether to follow method calls when building the model */
	protected final int m_follow;

	/**
	 * Creates a new runnable.
	 * @param context The thread context
	 * @param source The file source from which to read
	 * @param finders The set of finders to use
	 * @param found The set of found tokens
	 * @param quiet Whether to suppress warnings
	 * @param status A callback to report status
	 * @param follow Whether to follow method calls when building the model
	 */
	public SparqlTokenFinderRunnable(String project, FileSource source, Set<? extends TokenFinderFactory> finders, boolean quiet, StatusCallback status, int follow)
	{
		super(project, source.getFilename(), source, quiet, status, finders);
		m_follow = follow;
	}

	@Override
	protected void doRun(TokenFinderContext context, String code)
	{
		processFile(context, m_file, code, m_finders, m_quiet, m_follow);
	}

	/**
	 * Processes a single Java file to find assertions.
	 * @param p The Java parser to use
	 * @param file The file name
	 * @param code The contents of the file
	 * @param finders The set of finders to use
	 * @param found The set of found tokens
	 * @param quiet Whether to suppress warnings
	 * @param follow Whether to follow method calls when building the model
	 */
	protected void processFile(TokenFinderContext context, String file, String code, Set<? extends TokenFinderFactory> finders, boolean quiet, int follow)
	{
		try
		{
			CompilationUnit u = context.getParser().parse(code).getResult().get();
			PushPopVisitableNode pm = new PushPopVisitableNode(u);
			ModelBuilder.ModelBuilderResult r = ModelBuilder.buildModel(pm, follow, context);	    
			LazyNodeIndex<Expression,String> globalAstIndex = r.getIndex();
			for (TokenFinderFactory fac : finders)
			{
				if (!(fac instanceof SparqlTokenFinderFactory))
				{
					continue;
				}
				SparqlTokenFinder f = ((SparqlTokenFinderFactory) fac).newFinder();
				/*if (hasCache(f))
				{
					// Don't run the finder, just read from cache
					readCache(f);
				}
				else*/
				{
					// No cache, run the finder
					f.setModel(r.getModel());
					f.setIndex(globalAstIndex);
					f.setFilename(file);
					f.setContext(context);
					f.process();
					m_found.addAll(f.getFoundTokens());
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

	@Override
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
