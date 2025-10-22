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

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import ca.uqac.lif.piglet.find.FoundToken;
import ca.uqac.lif.piglet.find.TokenFinderCallable;
import ca.uqac.lif.piglet.find.TokenFinderContext;
import ca.uqac.lif.piglet.find.TokenFinderFactory;
import ca.uqac.lif.piglet.find.TokenFinder.TokenFinderException;
import ca.uqac.lif.piglet.provider.FileSource;
import ca.uqac.lif.piglet.util.StatusCallback;

/**
 * A runnable that processes a single Java file to find assertions.
 */
public class VisitorAssertionFinderCallable extends TokenFinderCallable
{		
	/**
	 * Creates a new runnable.
	 * @param project The project name
	 * @param source The file source from which to read
	 * @param finders The set of finders to use
	 * @param found The set of found tokens
	 * @param quiet Whether to suppress warnings
	 * @param status A callback to report status
	 */
	public VisitorAssertionFinderCallable(String project, FileSource source, Set<VisitorAssertionFinderFactory> finders, boolean quiet, StatusCallback status)
	{
		super(project, source.getFilename(), source, quiet, status, finders);
	}
	
	@Override
	protected void doRun(TokenFinderContext context, String code, Set<FoundToken> found) throws TokenFinderException
	{
		try
		{
			CompilationUnit u = context.getParser().parse(code).getResult().get();
			LexicalPreservingPrinter.setup(u);
			List<MethodDeclaration> methods = getTestCases(u);
			/*if (methods.isEmpty() && !quiet)
			{
				// No test cases in this file
				System.err.println("WARNING: No test cases found in " + file);
			}*/
			for (MethodDeclaration m : methods)
			{
				PushPopVisitableNode pm = new PushPopVisitableNode(m);
				for (TokenFinderFactory t_factory : m_finders)
				{
					if (!(t_factory instanceof VisitorAssertionFinderFactory))
					{
						continue;
					}
					VisitorAssertionFinder new_f = (VisitorAssertionFinder) t_factory.newFinder();
					new_f.setFilename(m_file);
					new_f.setContext(context);
					pm.accept(new_f);
					found.addAll(new_f.getFoundTokens());
				}
			}
		}
		catch (ParseProblemException e)
		{
			throw new TokenFinderException("Error parsing " + m_file + ": " + e.getMessage(), e);
		}
		catch (NoSuchElementException e)
		{
			// Ignore this file
			if (!m_quiet)
			{
				System.err.println("Could not parse " + m_file);
			}
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
	protected void processFile(TokenFinderContext context, String file, String code, Set<? extends TokenFinderFactory> finders, boolean quiet)
	{
		
	}
}
