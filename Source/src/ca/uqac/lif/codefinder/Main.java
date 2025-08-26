package ca.uqac.lif.codefinder;

import static org.codelibs.jhighlight.renderer.XhtmlRendererFactory.JAVA;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.codelibs.jhighlight.renderer.Renderer;
import org.codelibs.jhighlight.renderer.XhtmlRendererFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import ca.uqac.lif.codefinder.AnsiPrinter.Color;
import ca.uqac.lif.codefinder.assertion.AnyAssertionFinder;
import ca.uqac.lif.codefinder.assertion.AssertionFinder;
import ca.uqac.lif.codefinder.assertion.CompoundAssertionFinder;
import ca.uqac.lif.codefinder.assertion.ConditionalAssertionFinder;
import ca.uqac.lif.codefinder.assertion.EqualAssertionFinder;
import ca.uqac.lif.codefinder.assertion.FoundToken;
import ca.uqac.lif.codefinder.assertion.IteratedAssertionFinder;
import ca.uqac.lif.codefinder.assertion.EqualNonPrimitiveFinder;
import ca.uqac.lif.codefinder.assertion.EqualStringFinder;
import ca.uqac.lif.codefinder.assertion.EqualityWithMessageFinder;
import ca.uqac.lif.codefinder.provider.FileProvider;
import ca.uqac.lif.codefinder.provider.FileSource;
import ca.uqac.lif.codefinder.provider.FileSystemProvider;
import ca.uqac.lif.codefinder.provider.UnionProvider;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.FileUtils;
import ca.uqac.lif.fs.HardDisk;
import ca.uqac.lif.util.CliParser;
import ca.uqac.lif.util.CliParser.Argument;
import ca.uqac.lif.util.CliParser.ArgumentMap;

public class Main
{
	public static void main(String[] args) throws FileSystemException, IOException
	{
		AnsiPrinter stderr = new AnsiPrinter(System.err);
		AnsiPrinter stdout = new AnsiPrinter(System.out);

		int num_threads = 2;
		boolean quiet = false;
		boolean summary = false;

		/* Setup command line options */
		CliParser cli = setupCli();
		ArgumentMap map = cli.parse(args);
		String output_file = "/tmp/report.html";
		String source_path = null;
		if (map.containsKey("no-color"))
		{
			stdout.disableColors();
			stderr.disableColors();
		}
		if (map.containsKey("quiet"))
		{
			quiet = true;
		}
		if (map.containsKey("summary"))
		{
			summary = true;
		}
		if (map.containsKey("output"))
		{
			output_file = map.getOptionValue("output");
		}
		if (map.containsKey("s"))
		{
			source_path = map.getOptionValue("source");
		}
		if (map.hasOption("threads"))
		{
			num_threads = Integer.parseInt(map.getOptionValue("threads").trim());
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
		//JavaParser parser = new JavaParser(parserConfiguration);

		/* Setup the file provider */
		List<String> folders = map.getOthers(); // The files to read from
		FileSystemProvider[] providers = new FileSystemProvider[folders.size()];
		for (int i = 0; i < folders.size(); i++)
		{
			providers[i] = new FileSystemProvider(new HardDisk(folders.get(i)));
		}
		UnionProvider fsp = new UnionProvider(providers);
		int total = fsp.filesProvided();
		Map<String,List<FoundToken>> categorized = new ConcurrentHashMap<>();
		Set<FoundToken> found = Collections.synchronizedSet(new HashSet<>());
		Runtime.getRuntime().addShutdownHook(new Thread(new EndRunnable(stdout, categorized, summary)));

		// Instantiate assertion finders
		Set<AssertionFinder> finders = new HashSet<AssertionFinder>();
		finders.add(new AnyAssertionFinder(null));
		finders.add(new CompoundAssertionFinder(null));
		finders.add(new ConditionalAssertionFinder(null));
		finders.add(new EqualAssertionFinder(null));
		finders.add(new IteratedAssertionFinder(null));
		finders.add(new EqualNonPrimitiveFinder(null));
		finders.add(new EqualStringFinder(null));
		finders.add(new EqualityWithMessageFinder(null));

		// Read file(s)
		StatusCallback status = new StatusCallback(stdout, total);
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(num_threads);
		processBatch(executor, parserConfiguration, fsp, finders, found, quiet, status);
		executor.shutdown();
		try
		{
			if (!executor.awaitTermination(120, TimeUnit.SECONDS))
			{
				executor.shutdownNow();
				if (!executor.awaitTermination(120, TimeUnit.SECONDS))
				{
					stderr.println("Cannot terminate process");
				}
			}
		}
		catch (InterruptedException e)
		{
			// (Re-)Cancel if current thread also interrupted
			executor.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
		stdout.print("\r\033[2K");
		stdout.println(fsp.filesProvided() + " file(s) analyzed");
		stdout.println(found.size() + " assertion(s) found");
		stdout.println();
		categorize(categorized, found);
		HardDisk hd = new HardDisk("/").open();
		createReport(new PrintStream(hd.writeTo(output_file)), categorized);
		hd.close();
	}

	protected static CliParser setupCli()
	{
		CliParser cli = new CliParser();
		cli.addArgument(new Argument().withShortName("o").withLongName("output").withDescription("Output file (default: report.html)").withArgument("file"));
		cli.addArgument(new Argument().withShortName("s").withLongName("source").withDescription("Additional source in path").withArgument("path"));
		cli.addArgument(new Argument().withShortName("t").withLongName("threads").withArgument("n").withDescription("Use up to n threads"));
		cli.addArgument(new Argument().withShortName("q").withLongName("quiet").withDescription("Do not show error messages"));
		cli.addArgument(new Argument().withShortName("m").withLongName("summary").withDescription("Only show a summary at the CLI"));
		cli.addArgument(new Argument().withShortName("c").withLongName("no-color").withDescription("Disable colored output"));
		return cli;
	}

	protected static void processBatch(Executor e, ParserConfiguration conf, FileProvider provider, Set<AssertionFinder> finders, Set<FoundToken> found, boolean quiet, StatusCallback status) throws IOException, FileSystemException
	{
		while (provider.hasNext())
		{
			FileSource fs = provider.next();
			InputStream stream = fs.getStream();
			String code = new String(FileUtils.toBytes(stream));
			stream.close();
			e.execute(new AssertionFinderRunnable(new JavaParser(conf), fs.getFilename(), code, finders, found, quiet, status));
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

	protected static void createReport(PrintStream out, Map<String,List<FoundToken>> found) throws IOException
	{ 
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<head>");
		printHighlightCss(out);
		out.println("</head>");
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
			if (e.getKey().compareTo(AnyAssertionFinder.NAME) != 0)
			{
				out.println("<h2><a name=\"" + e.getKey() + "\"></a>" + e.getKey() + " (" + e.getValue().size() + ")</h2>");
				reportTokens(out, e.getValue());
			}
		}
		out.println("</body>");
		out.println("</html>");
	}

	protected static void printHighlightCss(PrintStream out)
	{
		out.println("<style type=\"text/css\">\n" + "code {\n"
				+ "color: rgb(0,0,0); font-family: monospace; font-size: 12px; white-space: nowrap;\n"
				+ "}\n" + ".java_plain {\n" + "color: rgb(0,0,0);\n"
				+ "}\n" + ".java_keyword {\n"
				+ "color: rgb(0,0,0); font-weight: bold;\n" + "}\n"
				+ ".java_javadoc_tag {\n"
				+ "color: rgb(147,147,147); background-color: rgb(247,247,247); font-style: italic; font-weight: bold;\n"
				+ "}\n" + "h1 {\n"
				+ "font-family: sans-serif; font-size: 16pt; font-weight: bold; color: rgb(0,0,0); background: rgb(210,210,210); border: solid 1px black; padding: 5px; text-align: center;\n"
				+ "}\n" + ".java_type {\n" + "color: rgb(0,44,221);\n"
				+ "}\n" + ".java_literal {\n" + "color: rgb(188,0,0);\n"
				+ "}\n" + ".java_javadoc_comment {\n"
				+ "color: rgb(147,147,147); background-color: rgb(247,247,247); font-style: italic;\n"
				+ "}\n" + ".java_operator {\n"
				+ "color: rgb(0,124,31);\n" + "}\n"
				+ ".java_separator {\n" + "color: rgb(0,33,255);\n"
				+ "}\n" + ".java_comment {\n"
				+ "color: rgb(147,147,147); background-color: rgb(247,247,247);\n"
				+ "}\n" + "    </style>\n");
	}

	protected static void reportTokens(PrintStream out, List<FoundToken> found) throws IOException
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
			String code = t.getSnippet();
			Renderer rend = XhtmlRendererFactory.getRenderer(JAVA);
			String html = rend.highlight("", code, "utf-8", true);;
			out.println("<dd><pre>" + html + "</pre></dd>");
		}
		out.println("</dl>");
	}

	protected static void displayTokens(AnsiPrinter out, List<FoundToken> found)
	{
		Collections.sort(found);
		for (FoundToken t : found)
		{
			out.print("- ");
			out.println(t);
		}
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

		private final AnsiPrinter m_stdout;

		private final boolean m_summary;

		public EndRunnable(AnsiPrinter stdout, Map<String,List<FoundToken>> found, boolean summary)
		{
			super();
			m_found = found;
			m_stdout = stdout;
			m_summary = summary;
		}

		@Override
		public void run()
		{
			displayResults();
		}

		protected void displayResults()
		{
			for (Map.Entry<String, List<FoundToken>> e : m_found.entrySet())
			{
				m_stdout.print(AnsiPrinter.padToLength(e.getKey(), 36));
				m_stdout.setForegroundColor(Color.DARK_GRAY);
				m_stdout.print(": ");
				m_stdout.setForegroundColor(Color.YELLOW);
				m_stdout.println(e.getValue().size());
				m_stdout.resetColors();
				if (!m_summary)
				{
					displayTokens(m_stdout, e.getValue());
					m_stdout.println();
				}
			}
		}
	}
}
