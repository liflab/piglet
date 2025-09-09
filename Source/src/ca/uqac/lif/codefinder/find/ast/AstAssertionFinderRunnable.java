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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import ca.uqac.lif.codefinder.Main;
import ca.uqac.lif.codefinder.find.TokenFinderContext;
import ca.uqac.lif.codefinder.find.ast.AstAssertionFinder.AstAssertionFinderFactory;
import ca.uqac.lif.codefinder.provider.FileSource;
import ca.uqac.lif.codefinder.thread.AssertionFinderRunnable;
import ca.uqac.lif.codefinder.util.StatusCallback;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.FileUtils;

/**
 * A runnable that processes a single Java file to find assertions.
 */
public class AstAssertionFinderRunnable extends AssertionFinderRunnable
{
	
	/** The set of finders to use */
	protected final Set<AstAssertionFinderFactory> m_finders;
		
	/**
	 * Creates a new runnable.
	 * @param context The thread context
	 * @param source The file source from which to read
	 * @param finders The set of finders to use
	 * @param found The set of found tokens
	 * @param quiet Whether to suppress warnings
	 * @param status A callback to report status
	 */
	public AstAssertionFinderRunnable(FileSource source, Set<AstAssertionFinderFactory> finders, boolean quiet, StatusCallback status)
	{
		super(source.getFilename(), source, quiet, status);
		m_finders = finders;
	}
	
	@Override
	public void run()
	{
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
	protected void processFile(TokenFinderContext context, String file, String code, Set<AstAssertionFinderFactory> finders, boolean quiet)
	{
		try
		{
			CompilationUnit u = context.getParser().parse(code).getResult().get();
			List<MethodDeclaration> methods = getTestCases(u);
			/*if (methods.isEmpty() && !quiet)
			{
				// No test cases in this file
				System.err.println("WARNING: No test cases found in " + file);
			}*/
			for (MethodDeclaration m : methods)
			{
				PushPopVisitableNode pm = new PushPopVisitableNode(m);
				for (AstAssertionFinderFactory factory : finders)
				{
					AstAssertionFinder new_f = factory.newFinder();
					new_f.setFilename(file);
					new_f.setContext(context);
					pm.accept(new_f);
					m_found.addAll(new_f.getFoundTokens());
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
}
