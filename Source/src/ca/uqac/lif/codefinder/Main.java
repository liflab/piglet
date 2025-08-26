package ca.uqac.lif.codefinder;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import ca.uqac.lif.codefinder.assertion.AnyAssertionFinder;
import ca.uqac.lif.codefinder.assertion.AssertionFinder;
import ca.uqac.lif.codefinder.assertion.CompoundAssertionFinder;
import ca.uqac.lif.codefinder.assertion.ConditionalAssertionFinder;
import ca.uqac.lif.codefinder.assertion.EqualAssertionFinder;
import ca.uqac.lif.codefinder.assertion.FoundToken;
import ca.uqac.lif.codefinder.assertion.IteratedAssertionFinder;
import ca.uqac.lif.codefinder.assertion.EqualNonPrimitiveFinder;
import ca.uqac.lif.codefinder.assertion.EqualStringFinder;
import ca.uqac.lif.codefinder.provider.FileProvider;
import ca.uqac.lif.codefinder.provider.FileSource;
import ca.uqac.lif.codefinder.provider.FileSystemProvider;
import ca.uqac.lif.codefinder.provider.UnionProvider;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.HardDisk;
import ca.uqac.lif.util.CliParser;
import ca.uqac.lif.util.CliParser.Argument;
import ca.uqac.lif.util.CliParser.ArgumentMap;

public class Main
{
	public static void main(String[] args) throws FileSystemException, IOException
	{
		/* Setup command line options */
		CliParser cli = new CliParser();
		cli.addArgument(new Argument().withShortName("o").withLongName("output").withDescription("Output file (default: report.html)").withArgument("file"));
		cli.addArgument(new Argument().withShortName("s").withLongName("source").withDescription("Additional source in path").withArgument("path"));
		ArgumentMap map = cli.parse(args);
		String output_file = "/tmp/report.html";
		String source_path = null;
		if (map.containsKey("o"))
		{
			output_file = map.getOptionValue("o");
		}
		if (map.containsKey("s"))
		{
			source_path = map.getOptionValue("s");
		}
		
		/* Setup parser (boilerplate code) */
		CombinedTypeSolver typeSolver = new CombinedTypeSolver();
		typeSolver.add(new ReflectionTypeSolver());
		if (source_path != null)
		{
			typeSolver.add(new JavaParserTypeSolver(source_path));
		}
		ParserConfiguration parserConfiguration =
				new ParserConfiguration().setSymbolResolver(
						new JavaSymbolSolver(typeSolver));
		JavaParser parser = new JavaParser(parserConfiguration);
		
		/* Setup the file provider */
		List<String> files = map.getOthers(); // The files to read from
		FileSystemProvider[] providers = new FileSystemProvider[files.size()];
		for (int i = 0; i < files.size(); i++)
		{
			providers[i] = new FileSystemProvider(new HardDisk(files.get(i)));
		}
		UnionProvider fsp = new UnionProvider(providers);
		Map<String,List<FoundToken>> categorized = new ConcurrentHashMap<>();
		Set<FoundToken> found = Collections.synchronizedSet(new HashSet<>());
		Runtime.getRuntime().addShutdownHook(new Thread(new EndRunnable(categorized)));
		
		// Instantiate assertion finders
		Set<AssertionFinder> finders = new HashSet<AssertionFinder>();
		finders.add(new AnyAssertionFinder(null));
		finders.add(new CompoundAssertionFinder(null));
		finders.add(new ConditionalAssertionFinder(null));
		finders.add(new EqualAssertionFinder(null));
		finders.add(new IteratedAssertionFinder(null));
		finders.add(new EqualNonPrimitiveFinder(null));
		finders.add(new EqualStringFinder(null));

		// Read file(s)
		
		processBatch(parser, fsp, finders, found);
		System.out.println(fsp.filesProvided() + " file(s) analyzed");
		System.out.println(found.size() + " assertion(s) found");
		System.out.println();
		categorize(categorized, found);
		//displayResults(System.out, categorized);
		HardDisk hd = new HardDisk("/").open();
		createReport(new PrintStream(hd.writeTo(output_file)), categorized);
		hd.close();
	}

	protected static void processBatch(JavaParser p, FileProvider provider, Set<AssertionFinder> finders, Set<FoundToken> found) throws IOException
	{
		while (provider.hasNext())
		{
			FileSource fs = provider.next();
			InputStream stream = fs.getStream();
			processFile(p, fs.getFilename(), stream, finders, found);
			stream.close();
		}
	}
	
	protected static void categorize(Map<String,List<FoundToken>> map, Set<FoundToken> found)
	{
		for (FoundToken t : found)
		{
			addToMap(map, t);
		}
	}
	
	protected static void addToMap(Map<String, List<FoundToken>> map, FoundToken t)
	{
		List<FoundToken> list = null;
		if (map.containsKey(t.getAssertionName()))
		{
			list = map.get(t.getAssertionName());
		}
		else
		{
			list = new ArrayList<FoundToken>();
			map.put(t.getAssertionName(), list);
		}
		list.add(t);
	}

	protected static void createReport(PrintStream out, Map<String,List<FoundToken>> found)
	{ 
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<body>");
		out.println("<h2>Summary</h2>");
		out.println("<ul>");
		for (Map.Entry<String, List<FoundToken>> e : found.entrySet())
		{
			out.println("<li>" + e.getKey() + " (" + e.getValue().size() + ")</li>");
		}
		out.println("</ul>");
		for (Map.Entry<String, List<FoundToken>> e : found.entrySet())
		{
			out.println("<h2><a name=\"" + e.getKey() + "\"></a>" + e.getKey() + " (" + e.getValue().size() + ")</h2>");
			reportTokens(out, e.getValue());
		}
		out.println("</body>");
		out.println("</html>");
	}

	protected static void reportTokens(PrintStream out, List<FoundToken> found)
	{
		out.println("<dl>");
		for (FoundToken t : found)
		{
			String clear_fn = t.getFilename().substring(1);
			out.print("<dt><a href=\"");
			out.print(clear_fn);
			out.print("\">");
			out.print(clear_fn);
			out.print("</a> ");
			out.print(t.getLocation());
			out.println("</dt>");
			out.println("<dd><pre>" + escape(t.getSnippet()) + "</pre></dd>");
		}
		out.println("</dl>");
	}

	protected static void displayResults(PrintStream out, Map<String,List<FoundToken>> found)
	{
		for (Map.Entry<String, List<FoundToken>> e : found.entrySet())
		{
			out.println(e.getKey() + " : " + e.getValue().size());
			displayTokens(out, e.getValue());
			out.println();
		}
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

	protected static void processFile(JavaParser p, String file, InputStream is, Set<AssertionFinder> finders, Set<FoundToken> found)
	{
		try
		{
			CompilationUnit u = p.parse(is).getResult().get();
			List<MethodDeclaration> methods = getTestCases(u);
			if (methods.isEmpty())
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
			System.err.println("Could not parse " + file);
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
	
	protected static class EndRunnable implements Runnable
	{
		private final Map<String,List<FoundToken>> m_found;
		
		public EndRunnable(Map<String,List<FoundToken>> found)
		{
			super();
			m_found = found;
		}
		
		@Override
		public void run()
		{
			displayResults(System.out, m_found);
		}
	}

}
