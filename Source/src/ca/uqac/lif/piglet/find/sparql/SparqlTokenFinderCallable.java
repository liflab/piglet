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
package ca.uqac.lif.piglet.find.sparql;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.ParseProblemException;

import ca.uqac.lif.piglet.find.FoundToken;
import ca.uqac.lif.piglet.find.TokenFinderCallable;
import ca.uqac.lif.piglet.find.TokenFinderContext;
import ca.uqac.lif.piglet.find.TokenFinderFactory;
import ca.uqac.lif.piglet.find.TokenFinder.TokenFinderException;
import ca.uqac.lif.piglet.find.visitor.PushPopVisitableNode;
import ca.uqac.lif.piglet.provider.FileSource;
import ca.uqac.lif.piglet.util.StatusCallback;

/**
 * A runnable that processes a single Java file to find assertions.
 */
public class SparqlTokenFinderCallable extends TokenFinderCallable
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
	public SparqlTokenFinderCallable(String project, FileSource source, Set<? extends TokenFinderFactory> finders, boolean quiet, StatusCallback status, int follow)
	{
		super(project, source.getFilename(), source, quiet, status, finders);
		m_follow = follow;
	}

	@Override
	protected void doRun(TokenFinderContext context, String code, Set<FoundToken> found) throws TokenFinderException
	{
		Set<FoundToken> in_found = processFile(context, m_file, code, m_finders, m_quiet, m_follow);
		found.addAll(in_found);
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
	 * @throws TokenFinderException If an error occurs during processing
	 */
	protected Set<FoundToken> processFile(TokenFinderContext context, String file, String code, Set<? extends TokenFinderFactory> finders, boolean quiet, int follow) throws TokenFinderException
	{
		Set<FoundToken> found = new HashSet<>();
		Set<SparqlTokenFinderFactory> localFinders = new HashSet<>();
		for (TokenFinderFactory fac : finders)
		{
			if (fac instanceof SparqlTokenFinderFactory)
			{
				localFinders.add((SparqlTokenFinderFactory) fac);
				fac.registerExpected();
			}
		}
		if (localFinders.isEmpty())
		{
			return found;
		}
		try
		{
			CompilationUnit cu = context.getParser().parse(code).getResult().get();
			LexicalPreservingPrinter.setup(cu);
			PushPopVisitableNode pm = new PushPopVisitableNode(cu);
			ModelBuilder.ModelBuilderResult r = ModelBuilder.buildModel(pm, follow, context, file);
			if(Thread.currentThread().isInterrupted()) { 
				return found;
			}
			LazyNodeIndex<Node,String> globalAstIndex = r.getIndex();
			for (SparqlTokenFinderFactory fac : localFinders)
			{
				if(Thread.currentThread().isInterrupted()) { 
					return found;
				}
				SparqlTokenFinder f = (SparqlTokenFinder) fac.newFinder();
				f.setModel(r.getModel());
				f.setIndex(globalAstIndex);
				f.setFilename(file);
				f.setContext(context);
				f.process();
				fac.registerFinished();
				found.addAll(f.getFoundTokens());
			}
		}
		catch (ParseProblemException e)
		{
			throw new TokenFinderException("Error parsing " + file + ": " + e.getMessage(), e);
		}
		catch (NoSuchElementException e)
		{
			// Ignore this file
			if (!quiet)
			{
				System.err.println("Could not parse " + file);
			}
		}
		return found;
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
