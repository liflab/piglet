package ca.uqac.lif.codefinder;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import ca.uqac.lif.codefinder.assertion.AssertionFinder;
import ca.uqac.lif.codefinder.assertion.FoundToken;

public class AssertionFinderRunnable implements Runnable
{
	protected final JavaParser m_parser;
	
	protected final String m_file;
	
	protected final String m_is;
	
	protected final Set<AssertionFinder> m_finders;
	
	protected final Set<FoundToken> m_found;
	
	protected final boolean m_quiet;
	
	protected final StatusCallback m_callback;
	
	public AssertionFinderRunnable(JavaParser p, String file, String code, Set<AssertionFinder> finders, Set<FoundToken> found, boolean quiet, StatusCallback status)
	{
		super();
		m_parser = p;
		m_file = file;
		m_is = code;
		m_finders = finders;
		m_found = found;
		m_quiet = quiet;
		m_callback = status;
	}
	
	@Override
	public void run()
	{
		processFile(m_parser, m_file, m_is, m_finders, m_found, m_quiet);
		if (m_callback != null)
		{
			m_callback.done();
		}
	}
	
	public static void processFile(JavaParser p, String file, String code, Set<AssertionFinder> finders, Set<FoundToken> found, boolean quiet)
	{
		try
		{
			CompilationUnit u = p.parse(code).getResult().get();
			List<MethodDeclaration> methods = getTestCases(u);
			if (methods.isEmpty() && !quiet)
			{
				// No test cases in this file
				System.err.println("WARNING: No test cases found in " + file);
			}
			for (MethodDeclaration m : methods)
			{
				for (AssertionFinder f : finders)
				{
					AssertionFinder new_f = f.newFinder(file);
					new_f.visit(m, found);
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
