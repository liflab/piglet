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
import ca.uqac.lif.codefinder.util.AnsiPrinter;
import ca.uqac.lif.codefinder.util.StatusCallback;
import ca.uqac.lif.codefinder.util.AnsiPrinter.Color;
import ca.uqac.lif.fs.FilePath;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.FileUtils;
import ca.uqac.lif.fs.HardDisk;
import ca.uqac.lif.util.CliParser;
import ca.uqac.lif.util.CliParser.Argument;
import ca.uqac.lif.util.CliParser.ArgumentMap;

/**
 * Main class of the CodeFinder application. Parses command line arguments,
 * sets up the environment, and launches the analysis.
 */
public class Main
{
	/**
	 * Main entry point of the application
	 * @param args Command line arguments
	 * @throws FileSystemException When a file system error occurs
	 * @throws IOException When an I/O error occurs
	 */
	public static void main(String[] args) throws FileSystemException, IOException
	{
		AnsiPrinter stderr = new AnsiPrinter(System.err);
		AnsiPrinter stdout = new AnsiPrinter(System.out);

		int num_threads = 2;
		boolean quiet = false;
		boolean summary = false;
		int limit = -1;

		/* Setup command line options */
		CliParser cli = setupCli();
		ArgumentMap map = cli.parse(args);
		String output_file = "report.html";
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
		if (map.hasOption("limit"))
		{
			limit = Integer.parseInt(map.getOptionValue("limit").trim());
		}
		if (map.containsKey("no-color"))
		{
			stdout.disableColors();
			stderr.disableColors();
		}
		
		/* The path in which the executable is executed */
		FilePath home_path = new FilePath(System.getProperty("user.dir"));

		/* Setup the file provider */
		List<String> folders = map.getOthers(); // The files to read from
		FileSystemProvider[] providers = new FileSystemProvider[folders.size()];
		for (int i = 0; i < folders.size(); i++)
		{
			FilePath fold_path = home_path.chdir(new FilePath(folders.get(i)));
			providers[i] = new FileSystemProvider(new HardDisk(fold_path.toString()));
		}
		UnionProvider fsp = new UnionProvider(providers);
		int total = fsp.filesProvided();
		Map<String,List<FoundToken>> categorized = new ConcurrentHashMap<>();
		Set<FoundToken> found = Collections.synchronizedSet(new HashSet<>());
		Runtime.getRuntime().addShutdownHook(new Thread(new EndRunnable(stdout, categorized, summary)));
		
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
		processBatch(executor, parserConfiguration, fsp, finders, found, quiet, status, limit);
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
		
		/* Categorize results and produce report */
		categorize(categorized, found);
		FilePath output_path = home_path.chdir(getPathOfFile(output_file));
		FilePath reverse_path = output_path.chdir(new FilePath(folders.get(0)));
		HardDisk hd = new HardDisk(output_path.toString()).open();
		createReport(reverse_path, new PrintStream(hd.writeTo(getFilename(output_file))), categorized);
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
		cli.addArgument(new Argument().withShortName("l").withLongName("limit").withArgument("n").withDescription("Stop after n files (for testing purposes)"));
		return cli;
	}

	protected static void processBatch(Executor e, ParserConfiguration conf, FileProvider provider, Set<AssertionFinder> finders, Set<FoundToken> found, boolean quiet, StatusCallback status, int limit) throws IOException, FileSystemException
	{
		int count = 0;
		while (provider.hasNext() && (limit == -1 || count < limit))
		{
			count++;
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

	

	

	protected static void displayTokens(AnsiPrinter out, List<FoundToken> found)
	{
		Collections.sort(found);
		for (FoundToken t : found)
		{
			out.print("- ");
			out.println(t);
		}
	}
	
	public static FilePath getPathOfFile(String path)
	{
		int last_slash = path.lastIndexOf('/');
		if (last_slash == -1)
		{
			last_slash = path.lastIndexOf('\\');
		}
		FilePath path_f = null;
		if (last_slash != -1)
		{
			path_f = new FilePath(path.substring(0, last_slash));
		}
		else
		{
			path_f = new FilePath(".");
		}
		if (path_f.toString().isEmpty())
		{
			path_f = new FilePath(".");
		}
		return path_f;
	}
	
	public static String getFilename(String path)
	{
		int last_slash = path.lastIndexOf('/');
		if (last_slash == -1)
		{
			last_slash = path.lastIndexOf('\\');
		}
		String name = null;
		if (last_slash != -1)
		{
			name = path.substring(last_slash + 1);
		}
		else
		{
			name = path;
		}
		return name;
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
