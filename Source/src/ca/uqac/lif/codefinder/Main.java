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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
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
import ca.uqac.lif.codefinder.report.CliReporter;
import ca.uqac.lif.codefinder.report.HtmlReporter;
import ca.uqac.lif.codefinder.util.AnsiPrinter;
import ca.uqac.lif.codefinder.util.Solvers;
import ca.uqac.lif.codefinder.util.StatusCallback;
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
	/** Return code indicating "no return" **/
	public static final int RET_NOTHING = -1;
	
	/** Return code indicating successful execution */
	public static final int RET_OK = 0;

	/** Return code indicating a file system error */
	public static final int RET_FS = 1;

	/** Return code indicating an I/O error */
	public static final int RET_IO = 2;

	/** Standard output */
	protected static final AnsiPrinter s_stdout = new AnsiPrinter(System.out);

	/** Standard error */
	protected static final AnsiPrinter s_stderr = new AnsiPrinter(System.err);
	
	/** The path in which the executable is executed **/
	protected static final FilePath s_homePath = new FilePath(System.getProperty("user.dir"));
	
	/** Set of unresolved symbols **/
	protected static final Set<String> s_setUnresolved = Collections.synchronizedSet(new HashSet<String>());
	
	/** The parsed command line arguments **/
	protected static ArgumentMap s_map;

	/** Additional source paths */
	protected static Set<String> s_sourcePaths = new HashSet<>();
	
	/** Additional jar files */
	protected static Set<String> s_jarPaths = new HashSet<>();

	/** Name of the output file */
	protected static String s_outputFile = "report.html";
	
	/** Whether to operate in quiet mode (no error messages) */
	protected static boolean s_quiet = false;
	
	/** Whether to show unresolved symbols **/
	protected static boolean s_unresolved = false;

	/** Number of threads to use */
	protected static int s_threads = 2;
	
	/** The name of the root package to look for in the source tree **/
	public static String s_root = null;

	/** Whether to only show a summary at the command line */
	protected static boolean s_summary = false;

	/** Limit to the number of files to process (for testing purposes) */
	protected static int s_limit = -1;

	/**
	 * Main entry point of the application. This method simply calls
	 * {@link #doMain(String[])} and exits with the return code of that method.
	 * This is done so that {@link #doMain(String[])} could be called
	 * through unit tests without exiting the JVM.
	 * @param args Command line arguments
	 * @throws FileSystemException When a file system error occurs
	 * @throws IOException When an I/O error occurs
	 */
	public static void main(String[] args) throws FileSystemException, IOException
	{
		System.exit(doMain(args));
	}

	/**
	 * Main entry point of the application
	 * @param args Command line arguments
	 * @throws FileSystemException When a file system error occurs
	 * @throws IOException When an I/O error occurs
	 */
	public static int doMain(String[] args)
	{
		/* Setup command line options */
		CliParser cli = setupCli();
		s_map = cli.parse(args);
		int ret = processCommandLine(cli);
		if (ret != RET_NOTHING)
		{
			return ret;
		} 

		/* Setup the file provider */
		List<String> folders = s_map.getOthers(); // The files to read from
		FileSystemProvider[] providers = new FileSystemProvider[folders.size()];
		for (int i = 0; i < folders.size(); i++)
		{
			FilePath fold_path = s_homePath.chdir(new FilePath(folders.get(i)));
			try
			{
				providers[i] = new FileSystemProvider(new HardDisk(fold_path.toString()));
			}
			catch (FileSystemException e)
			{
				return RET_FS;
			}
		}
		UnionProvider fsp = new UnionProvider(providers);
		int total = fsp.filesProvided();
		Map<String,List<FoundToken>> categorized = new ConcurrentHashMap<>();
		Set<FoundToken> found = Collections.synchronizedSet(new HashSet<>());
		Runtime.getRuntime().addShutdownHook(new Thread(new EndRunnable(categorized, s_summary)));

		/* Setup parser (boilerplate code) */
    
		//ParserConfiguration parserConfiguration = JavaParserFactory.getConfiguration(new String[] {s_sourcePath});
		//ParserConfiguration parserConfiguration =
		//		new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
		CombinedTypeSolver typeSolver = null;
		try
		{
			typeSolver = Solvers.buildSolver(s_sourcePaths, s_root, s_jarPaths);
		}
		catch (IOException e)
		{
			s_stderr.println("Could not set up type solver");
			return RET_IO;
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ParserConfiguration parserConfiguration = Solvers.parserConfig(typeSolver);
		typeSolver.add(new ReflectionTypeSolver());
		for (String s_sourcePath : s_sourcePaths)
		{
			if (s_sourcePath != null)
			{
				typeSolver.add(new JavaParserTypeSolver(s_sourcePath, parserConfiguration));
			}
		}
		for (String s_jarPath : s_jarPaths)
		{
			if (s_jarPath != null)
			{
				try
				{
					typeSolver.add(new com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver(s_jarPath));
				}
				catch (IOException e)
				{
					s_stderr.println("Could not read jar file: " + s_jarPath);
					return RET_IO;
				}
			}
		}
		typeSolver.add(new ClassLoaderTypeSolver(Thread.currentThread().getContextClassLoader()));

		// Instantiate assertion finders
		Set<AssertionFinder> finders = new HashSet<AssertionFinder>();
		finders.add(new AnyAssertionFinder(null));
		finders.add(new CompoundAssertionFinder(null));
		finders.add(new ConditionalAssertionFinder(null));
		finders.add(new EqualAssertionFinder(null));
		finders.add(new IteratedAssertionFinder(null));
		finders.add(new EqualNonPrimitiveFinder(null, typeSolver, s_setUnresolved));
		finders.add(new EqualStringFinder(null));
		finders.add(new EqualityWithMessageFinder(null, typeSolver));

		// Read file(s)
		StatusCallback status = new StatusCallback(s_stdout, total);
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(s_threads);
		try
		{
			processBatch(executor, parserConfiguration, fsp, finders, found, s_quiet, status, s_limit);
		}
		catch (IOException e)
		{
			return RET_IO;
		}
		catch (FileSystemException e)
		{
			return RET_FS;
		}
		executor.shutdown();
		try
		{
			if (!executor.awaitTermination(120, TimeUnit.SECONDS))
			{
				executor.shutdownNow();
				if (!executor.awaitTermination(120, TimeUnit.SECONDS))
				{
					s_stderr.println("Cannot terminate process");
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
		s_stdout.print("\r\033[2K");
		s_stdout.println(fsp.filesProvided() + " file(s) analyzed");
		s_stdout.println(found.size() + " assertion(s) found");
		s_stdout.println();

		/* Categorize results and produce report */
		categorize(categorized, found);
		FilePath output_path = s_homePath.chdir(getPathOfFile(s_outputFile));
		FilePath reverse_path = output_path.chdir(new FilePath(folders.get(0)));
		HardDisk hd;
		try
		{
			hd = new HardDisk(output_path.toString()).open();
			HtmlReporter reporter = new HtmlReporter(new PrintStream(hd.writeTo(getFilename(s_outputFile)), true, "UTF-8"));
			reporter.report(reverse_path, categorized, s_setUnresolved);
			hd.close();
		}
		catch (IOException e)
		{
			return RET_IO;
		}
		catch (FileSystemException e)
		{
			return RET_FS;
		}
		return RET_OK;
	}

	/**
	 * Processes the command line arguments and sets the appropriate static
	 * variables.
	 * @param cli The command line parser
	 * @return A return code if the program should exit, or -1 to continue
	 */
	protected static int processCommandLine(CliParser cli)
	{
		ArgumentMap map = s_map;
		if (map.containsKey("profile"))
		{
			return readProfile(map.getOptionValue("profile"));
		}
		if (map.containsKey("no-color"))
		{
			s_stdout.disableColors();
			s_stderr.disableColors();
		}
		if (map.containsKey("quiet"))
		{
			s_quiet = true;
		}
		if (map.containsKey("unresolved"))
		{
			s_unresolved = true;
		}
		if (map.containsKey("summary"))
		{
			s_summary = true;
		}
		if (map.containsKey("output"))
		{
			s_outputFile = map.getOptionValue("output");
		}
		if (map.containsKey("source"))
		{
			String[] paths = map.getOptionValue("source").split(":");
			for (String p : paths)
			{
				s_sourcePaths.add(p);
			}
		}
		if (map.containsKey("jar"))
		{
			String[] paths = map.getOptionValue("jar").split(":");
			for (String p : paths)
			{
				s_jarPaths.add(p);
			}
		}
		if (map.hasOption("threads"))
		{
			s_threads = Integer.parseInt(map.getOptionValue("threads").trim());
		}
		if (map.hasOption("limit"))
		{
			s_limit = Integer.parseInt(map.getOptionValue("limit").trim());
		}
		if (map.hasOption("root"))
		{
			s_root = map.getOptionValue("root");
		}
		if (map.containsKey("help") || map.getOthers().size() == 0)
		{
			showUsage(cli);
			return RET_OK;
		}
		if (map.containsKey("help") || map.getOthers().size() == 0)
		{
			showUsage(cli);
			return RET_OK;
		}
		return RET_NOTHING;
	}
	
	protected static int readProfile(String filename)
	{
		FilePath output_path = s_homePath.chdir(getPathOfFile(filename));
		FilePath reverse_path = output_path.chdir(getPathOfFile(filename));
		try
		{
			HardDisk hd = new HardDisk(reverse_path.toString()).open();
			StringBuilder contents = new StringBuilder();
			Scanner scanner = new Scanner(hd.readFrom(getFilename(filename)));
			while (scanner.hasNextLine())
			{
				String line = scanner.nextLine().trim();
				if (line.isEmpty() || line.startsWith("#"))
					continue;
				contents.append(line).append(" ");
			}
			scanner.close();
			hd.close();
			String[] args = contents.toString().split(" ");
			CliParser parser = setupCli();
			s_map = parser.parse(args);
			return processCommandLine(parser);
		}
		catch (FileSystemException e)
		{
			return RET_FS;
		}
	}

	/**
	 * Sets up the command line interface parser with the appropriate options.
	 * @return The command line parser
	 */
	protected static CliParser setupCli()
	{
		CliParser cli = new CliParser();
		cli.addArgument(new Argument().withShortName("o").withLongName("output").withDescription("Output file (default: report.html)").withArgument("file"));
		cli.addArgument(new Argument().withShortName("s").withLongName("source").withDescription("Additional source in path").withArgument("path"));
		cli.addArgument(new Argument().withShortName("j").withLongName("jar").withDescription("Additional jar file(s) in path").withArgument("path"));
		cli.addArgument(new Argument().withShortName("t").withLongName("threads").withArgument("n").withDescription("Use up to n threads"));
		cli.addArgument(new Argument().withShortName("q").withLongName("quiet").withDescription("Do not show error messages"));
		cli.addArgument(new Argument().withShortName("m").withLongName("summary").withDescription("Only show a summary at the CLI"));
		cli.addArgument(new Argument().withShortName("c").withLongName("no-color").withDescription("Disable colored output"));
		cli.addArgument(new Argument().withShortName("l").withLongName("limit").withArgument("n").withDescription("Stop after n files (for testing purposes)"));
		cli.addArgument(new Argument().withShortName("h").withLongName("help").withDescription("Display this help message"));
		cli.addArgument(new Argument().withShortName("p").withLongName("profile").withArgument("file").withDescription("Get options from file"));
		cli.addArgument(new Argument().withShortName("u").withLongName("unresolved").withDescription("Show unresolved symbols"));
		cli.addArgument(new Argument().withShortName("r").withLongName("root").withArgument("p").withDescription("Search in source tree for package p"));
		return cli;
	}

	/**
	 * Displays usage information for the command line interface.
	 * @param out The output stream to which the usage information is sent
	 * @param cli The command line parser
	 */
	protected static void showUsage(CliParser cli)
	{
		cli.printHelp("", s_stdout);
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

		private final boolean m_summary;

		public EndRunnable(Map<String,List<FoundToken>> found, boolean summary)
		{
			super();
			m_found = found;
			m_summary = summary;
		}

		@Override
		public void run()
		{
			displayResults();
		}

		protected void displayResults()
		{
			CliReporter cli_reporter = new CliReporter(s_stdout, m_summary);
			try
			{
				cli_reporter.report(null, m_found, s_setUnresolved);
			}
			catch (IOException e)
			{
				// Ignore
			}
		}
	}
}
