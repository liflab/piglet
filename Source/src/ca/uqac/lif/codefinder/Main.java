package ca.uqac.lif.codefinder;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;

import ca.uqac.lif.codefinder.assertion.CompoundAssertionFinder;
import ca.uqac.lif.codefinder.assertion.ConditionalAssertionFinder;
import ca.uqac.lif.codefinder.assertion.EqualAssertionFinder;
import ca.uqac.lif.codefinder.assertion.FoundToken;
import ca.uqac.lif.codefinder.assertion.IteratedAssertionFinder;
import ca.uqac.lif.codefinder.assertion.CompoundAssertionFinder.CompoundAssertionToken;
import ca.uqac.lif.codefinder.assertion.ConditionalAssertionFinder.ConditionalAssertionToken;
import ca.uqac.lif.codefinder.assertion.EqualAssertionFinder.EqualAssertionToken;
import ca.uqac.lif.codefinder.assertion.IteratedAssertionFinder.IteratedAssertionToken;
import ca.uqac.lif.codefinder.provider.FileProvider;
import ca.uqac.lif.codefinder.provider.FileSource;
import ca.uqac.lif.codefinder.provider.FileSystemProvider;
import ca.uqac.lif.codefinder.provider.UnionProvider;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.HardDisk;

public class Main
{

	public static void main(String[] args) throws FileSystemException, IOException
	{
		// Setup parser
		TypeSolver typeSolver = new CombinedTypeSolver();
		ParserConfiguration parserConfiguration =
				new ParserConfiguration().setSymbolResolver(
						new JavaSymbolSolver(typeSolver));
		JavaParser parser = new JavaParser(parserConfiguration);

		// Read file
		UnionProvider fsp = new UnionProvider(
				//new FileSystemProvider(new HardDisk("/home/sylvain/Workspaces/repos/or-tools")),
				//new FileSystemProvider(new HardDisk("/home/sylvain/Workspaces/repos/okio"))
				new FileSystemProvider(new HardDisk("/home/sylvain/Workspaces/repos/intellij-community"))
				//new FileSystemProvider(new HardDisk("/home/sylvain/Workspaces/beepbeep/core/CoreTest/src")),
				//new FileSystemProvider(new HardDisk("/home/sylvain/Workspaces/lif-units/Source/srctest")),
				//new FileSystemProvider(new HardDisk("/home/sylvain/Workspaces/Azrael/Source"))
				);
		Set<FoundToken> found = new HashSet<>();
		processBatch(parser, fsp, found);
		System.out.println(fsp.filesProvided() + " file(s) analyzed");
		System.out.println();
		displayResults(System.out, found);
		HardDisk hd = new HardDisk("/tmp").open();
		createReport(new PrintStream(hd.writeTo("report.html")), found);
		hd.close();
	}

	protected static void processBatch(JavaParser p, FileProvider provider, Set<FoundToken> found) throws IOException
	{
		while (provider.hasNext())
		{
			FileSource fs = provider.next();
			InputStream stream = fs.getStream();
			processFile(p, fs.getFilename(), stream, found);
			stream.close();
		}
	}

	protected static void createReport(PrintStream out, Set<FoundToken> found)
	{
		List<FoundToken> compound = new ArrayList<FoundToken>();
		List<FoundToken> conditional = new ArrayList<FoundToken>();
		List<FoundToken> equal = new ArrayList<FoundToken>();
		List<FoundToken> iterated = new ArrayList<FoundToken>();
		for (FoundToken t : found)
		{
			if (t instanceof CompoundAssertionToken)
				compound.add(t);
			if (t instanceof ConditionalAssertionToken)
				conditional.add(t);
			if (t instanceof EqualAssertionToken)
				equal.add(t);
			if (t instanceof IteratedAssertionToken)
				iterated.add(t);
		}
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<body>");
		out.println("<h2>Summary</h2>");
		out.println("<ul>");
		out.println("<li><a href=\"#compound\">Compound assertions</a></li>");
		out.println("<li><a href=\"#conditional\">Conditional assertions</a></li>");
		out.println("<li><a href=\"#equal\">Equal assertions</a></li>");
		out.println("<li><a href=\"#iterated\">Iterated assertions</a></li>");
		out.println("</ul>");
		out.println("<h2><a name=\"compound\"></a>Compound assertions</h2>");
		reportTokens(out, compound);
		out.println("<h2><a name=\"conditional\"></a>Conditional assertions</h2>");
		reportTokens(out, conditional);
		out.println("<h2><a name=\"equal\"></a>Equal assertions</h2>");
		reportTokens(out, equal);
		out.println("<h2><a name=\"iterated\"></a>Iterated assertions</h2>");
		reportTokens(out, iterated);
		out.println("</body>");
		out.println("</html>");
	}

	protected static void reportTokens(PrintStream out, List<FoundToken> found)
	{
		out.println("<dl>");
		for (FoundToken t : found)
		{
			out.println("<dt>" + t + "</dt>");
			out.println("<dd><pre>" + escape(t.getSnippet()) + "</pre></dd>");
		}
		out.println("</dl>");
	}

	protected static void displayResults(PrintStream out, Set<FoundToken> found)
	{
		List<FoundToken> compound = new ArrayList<FoundToken>();
		List<FoundToken> conditional = new ArrayList<FoundToken>();
		List<FoundToken> equal = new ArrayList<FoundToken>();
		List<FoundToken> iterated = new ArrayList<FoundToken>();
		for (FoundToken t : found)
		{
			if (t instanceof CompoundAssertionToken)
				compound.add(t);
			if (t instanceof ConditionalAssertionToken)
				conditional.add(t);
			if (t instanceof EqualAssertionToken)
				equal.add(t);
			if (t instanceof IteratedAssertionToken)
				iterated.add(t);
		}
		out.println("Compound assertions");
		displayTokens(out, compound);
		out.println();
		out.println("Conditional assertions");
		displayTokens(out, conditional);
		out.println();
		out.println("Equal assertions");
		displayTokens(out, equal);
		out.println();
		out.println("Iterated assertions");
		displayTokens(out, iterated);
		out.println();
	}

	protected static void displayTokens(PrintStream out, List<FoundToken> found)
	{
		Collections.sort(found);
		for (FoundToken t : found)
		{
			out.print("- ");
			out.println(t);
		}
	}

	protected static void processFile(JavaParser p, String file, InputStream is, Set<FoundToken> found)
	{
		try
		{
			CompilationUnit u = p.parse(is).getResult().get();
			List<MethodDeclaration> methods = getTestCases(u);
			for (MethodDeclaration m : methods)
			{
				{
					ConditionalAssertionFinder finder = new ConditionalAssertionFinder(file);
					finder.visit(m, found);
				}
				{
					CompoundAssertionFinder finder = new CompoundAssertionFinder(file);
					finder.visit(m, found);
				}
				{
					IteratedAssertionFinder finder = new IteratedAssertionFinder(file);
					finder.visit(m, found);
				}
				{
					EqualAssertionFinder finder = new EqualAssertionFinder(file);
					finder.visit(m, found);
				}
			}
		}
		catch (NoSuchElementException e)
		{
			// Ignore this file
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

	protected static String escape(String s)
	{
		s = s.replace("&", "&amp;");
		s = s.replace("<", "&lt;");
		s = s.replace(">", "&gt;");
		return s;
	}

}
