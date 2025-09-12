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

import static ca.uqac.lif.codefinder.util.Paths.getFilename;
import static ca.uqac.lif.codefinder.util.Paths.getPathOfFile;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;

import bsh.EvalError;
import bsh.Interpreter;
import ca.uqac.lif.codefinder.find.FoundToken;
import ca.uqac.lif.codefinder.find.TokenFinderContext;
import ca.uqac.lif.codefinder.find.ast.AstAssertionFinder.AstAssertionFinderFactory;
import ca.uqac.lif.codefinder.find.ast.AstAssertionFinder;
import ca.uqac.lif.codefinder.find.ast.AstAssertionFinderRunnable;
import ca.uqac.lif.codefinder.find.sparql.SparqlAssertionFinderRunnable;
import ca.uqac.lif.codefinder.find.sparql.SparqlTokenFinder.SparqlTokenFinderFactory;
import ca.uqac.lif.codefinder.provider.FileProvider;
import ca.uqac.lif.codefinder.provider.FileSource;
import ca.uqac.lif.codefinder.provider.FileSystemProvider;
import ca.uqac.lif.codefinder.provider.UnionProvider;
import ca.uqac.lif.codefinder.report.CliReporter;
import ca.uqac.lif.codefinder.report.HtmlReporter;
import ca.uqac.lif.codefinder.thread.AssertionFinderRunnable;
import ca.uqac.lif.codefinder.util.AnsiPrinter;
import ca.uqac.lif.codefinder.util.Solvers;
import ca.uqac.lif.codefinder.util.StatusCallback;
import ca.uqac.lif.fs.FilePath;
import ca.uqac.lif.fs.FileSystem;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.FileUtils;
import ca.uqac.lif.fs.HardDisk;
import ca.uqac.lif.util.CliParser;
import ca.uqac.lif.util.CliParser.Argument;
import ca.uqac.lif.util.CliParser.ArgumentMap;

/**
 * Main class of the CodeFinder application. Parses command line arguments, sets
 * up the environment, and launches the analysis.
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

	/** Return code indicating a BeanShell error */
	public static final int RET_BSH = 3;

	/** Standard output */
	protected static final AnsiPrinter s_stdout = new AnsiPrinter(System.out);

	/** Standard error */
	protected static final AnsiPrinter s_stderr = new AnsiPrinter(System.err);

	/** The path in which the executable is executed **/
	protected static final FilePath s_homePath = new FilePath(System.getProperty("user.dir"));

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

	/**
	 * Number of threads to use. If not specified, use
	 * number of processors - 1. */
	protected static int s_threads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);

	/** The name of the root package to look for in the source tree **/
	public static String s_root = null;

	/** Whether to only show a summary at the command line */
	protected static boolean s_summary = false;

	/** Limit to the number of files to process (for testing purposes) */
	protected static int s_limit = -1;

	/** Timeout for type resolution operations (in milliseconds) */
	protected static long s_resolutionTimeout = 100;

	/** Thread-local context (parser, type solver, etc.) */
	public static ThreadLocal<TokenFinderContext> CTX;

	/** The set of assertion finders working on the AST */
	public static final Set<AstAssertionFinderFactory> s_astFinders = new HashSet<>();

	/** The set of assertion finders working through SPARQL queries */
	public static final Set<SparqlTokenFinderFactory> s_sparqlFinders = new HashSet<>();

	/** Pattern to extract the name of an assertion from a comment */
	protected static final Pattern s_namePat = Pattern.compile("Name:([^\\*]+)");

	/**
	 * Main entry point of the application. This method simply calls
	 * {@link #doMain(String[])} and exits with the return code of that method. This
	 * is done so that {@link #doMain(String[])} could be called through unit tests
	 * without exiting the JVM.
	 * 
	 * @param args
	 *          Command line arguments
	 * @throws FileSystemException
	 *           When a file system error occurs
	 * @throws IOException
	 *           When an I/O error occurs
	 */
	public static void main(String[] args) throws Exception, FileSystemException, IOException
	{
		System.exit(doMain(args));
	}

	/**
	 * Main entry point of the application
	 * 
	 * @param args
	 *          Command line arguments
	 * @throws Exception 
	 * @throws FileSystemException
	 *           When a file system error occurs
	 */
	public static int doMain(String[] args) throws Exception
	{
		// Force static init of tables for TokenTypes up-front
		try
		{
			com.github.javaparser.TokenTypes.isComment(0); // harmless probe
		}
		catch (Throwable ignored)
		{
		}

		/* Setup command line options */
		CliParser cli = setupCli();
		s_map = cli.parse(args);
		int ret = processCli(cli);
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
		Map<String, List<FoundToken>> categorized = new TreeMap<>();
		Set<FoundToken> found = new HashSet<>();
		Runtime.getRuntime().addShutdownHook(new Thread(new EndRunnable(categorized, s_summary)));

		CTX = ThreadLocal.withInitial(() -> {
			try {
				CombinedTypeSolver ts = Solvers.buildSolver(s_sourcePaths, s_root, s_jarPaths);

				// Wire parser to THIS thread’s solver
				ParserConfiguration threadPc = new ParserConfiguration()
						.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_11)
						.setSymbolResolver(new com.github.javaparser.symbolsolver.JavaSymbolSolver(ts));

				return new TokenFinderContext(
						ts,
						new JavaParser(threadPc),
						com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade.get(ts),
						s_resolutionTimeout
						);
			} catch (Exception e) {
				throw new RuntimeException("Failed to init per-thread context", e);
			}
		});
		
		// Read file(s)
		StatusCallback status = new StatusCallback(s_stdout, (s_limit >= 0 ? s_limit : total));
		Thread status_thread = new Thread(status);
		AtomicInteger THREAD_ID = new AtomicInteger(1);
		ThreadFactory tf = r -> {
			Thread t = new Thread(r);
			t.setName("anl-" + THREAD_ID.incrementAndGet());
			t.setDaemon(false);
			return t;
		};
		ExecutorService executor = Executors.newFixedThreadPool(s_threads, tf);
		long start_time = System.currentTimeMillis();
		long end_time = -1;
		s_stdout.hideCursor();
		status_thread.start();
		try
		{
			processBatch(executor, fsp, s_astFinders, s_sparqlFinders, found, s_quiet, status, s_limit);
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
		end_time = System.currentTimeMillis();
		long duration = end_time - start_time;
		status.cleanup();
		s_stdout.println((s_limit >= 0 ? s_limit : total) + " file(s) analyzed");
		s_stdout.println(found.size() + " assertion(s) found");
		s_stdout.println("Analysis time: " + formatDuration(duration));
		s_stdout.println();

		/* Categorize results and produce report */
		categorize(categorized, found);
		FilePath output_path = s_homePath.chdir(getPathOfFile(s_outputFile));
		FilePath reverse_path = output_path.chdir(new FilePath(folders.get(0)));
		HardDisk hd;
		try
		{
			hd = new HardDisk(output_path.toString()).open();
			HtmlReporter reporter = new HtmlReporter(
					new PrintStream(hd.writeTo(getFilename(s_outputFile)), true, "UTF-8"));
			reporter.report(reverse_path, categorized, new HashSet<String>());
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
	 * 
	 * @param cli
	 *          The command line parser
	 * @return A return code if the program should exit, or -1 to continue
	 */
	protected static int processCli(CliParser cli)
	{
		ArgumentMap map = s_map;
		int ret = -1;
		if (map.containsKey("profile"))
		{
			ret = readProfile(map.getOptionValue("profile"));
			if (ret != RET_NOTHING)
			{
				return ret;
			}
		}
		if (map.containsKey("query"))
		{
			String bsh_file = map.getOptionValue("query");
			try
			{
				HardDisk hd = new HardDisk(s_homePath.toString()).open();
				if (hd.isDirectory(bsh_file))
				{
					s_stdout.println("Reading queries from folder " + bsh_file);
					hd.pushd(bsh_file);
					{
						// First the BeanShell scripts
						List<String> files = FileUtils.ls(hd, "", "^.*bsh$");
						for (String f : files)
						{
							try
							{
								AstAssertionFinderFactory factory = readBeanshell(hd, bsh_file + "/" + f);
								s_astFinders.add(factory);
							}
							catch (FileSystemException e)
							{
								s_stderr.println("File system error while reading BeanShell script");
								return RET_FS;
							}
							catch (EvalError e)
							{
								s_stderr.println("Error while evaluating BeanShell script: " + e.getMessage());
								return RET_BSH;
							}
						}
					}
					{
						// Then the SPARQL scripts
						List<String> files = FileUtils.ls(hd, "", "^.*sparql$");
						for (String f : files)
						{
							try
							{
								SparqlTokenFinderFactory factory = readSparql(hd, bsh_file + "/" + f);
								s_sparqlFinders.add(factory);
							}
							catch (FileSystemException e)
							{
								s_stderr.println("File system error while reading SPARQL script");
								return RET_FS;
							}
							catch (IOException e)
							{
								s_stderr.println("I/O error while reading SPARQL script: " + e.getMessage());
								return RET_IO;
							}
						}
					}
					hd.popd();
				}
				else
				{
					if (bsh_file.endsWith(".sparql"))
					{
						s_stdout.println("Reading SPARQL query from file " + bsh_file);
						hd.pushd(getPathOfFile(bsh_file).toString());
						SparqlTokenFinderFactory factory = readSparql(hd, bsh_file);
						hd.popd();
						s_sparqlFinders.add(factory);
						return RET_NOTHING;
					}
					else if (bsh_file.endsWith(".bsh"))
					{
						s_stdout.println("Reading BeanShell script from file " + bsh_file);
						hd.pushd(getPathOfFile(bsh_file).toString());
						AstAssertionFinderFactory factory = readBeanshell(hd, bsh_file);
						hd.popd();
						s_astFinders.add(factory);
					}					
				}
			}
			catch (FileSystemException e)
			{
				s_stderr.println("File system error while reading BeanShell script");
				return RET_FS;
			}
			catch (EvalError e)
			{
				s_stderr.println("Error while evaluating BeanShell script: " + e.getMessage());
				return RET_BSH;
			}
			catch (IOException e)
			{
				s_stderr.println("I/O error while reading SPARQL script: " + e.getMessage());
				return RET_IO;
			}
		}
		if (map.containsKey("resolution-timeout"))
		{
			try
			{
				s_resolutionTimeout = Long.parseLong(map.getOptionValue("resolution-timeout").trim());
			}
			catch (NumberFormatException e)
			{
				s_stderr.println("Invalid resolution timeout: " + map.getOptionValue("resolution-timeout"));
				return RET_NOTHING;
			}
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
			s_stdout.println("Analysis limited to first " + s_limit + " files");
		}
		if (map.hasOption("root"))
		{
			s_root = map.getOptionValue("root");
		}
		if (map.containsKey("help"))
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
			return processCli(parser);
		}
		catch (FileSystemException e)
		{
			return RET_FS;
		}
	}

	protected static SparqlTokenFinderFactory readSparql(FileSystem hd, String filename) throws FileSystemException, IOException
	{
		StringBuilder sparql_code = new StringBuilder();
		String name = null;
		Scanner scanner = new Scanner(hd.readFrom(getFilename(filename)));
		while (scanner.hasNextLine())
		{
			String line = scanner.nextLine().trim();
			if (line.isEmpty())
				continue;
			if (line.startsWith("#"))
			{
				if (name == null)
				{
					Matcher mat = s_namePat.matcher(line);
					if (mat.find())
					{
						name = mat.group(1).trim();
					}
				}
				continue;
			}
			sparql_code.append(line).append("\n");
		}
		scanner.close();
		return new SparqlTokenFinderFactory(name == null ? "Unnamed SPARQL finder" : name, sparql_code.toString());
	}

	protected static AstAssertionFinderFactory readBeanshell(HardDisk hd, String filename) throws FileSystemException, EvalError
	{
		//FilePath bsh_path = s_homePath.chdir(getPathOfFile(filename));
		//hd.pushd(getPathOfFile(filename).toString());
		String bsh_code = FileUtils.readStringFrom(hd, getFilename(filename));
		//hd.popd();
		bsh_code.replaceAll("^\\s*void visit\\(", "public void visit(");
		bsh_code.replaceAll("^\\s*void leave\\(", "public void leave(");
		Interpreter interpreter = new Interpreter();
		// Use the same loader that sees your app’s classes
		ClassLoader appCl = Main.class.getClassLoader();
		interpreter.setClassLoader(appCl);
		Thread.currentThread().setContextClassLoader(appCl);
		StringBuilder code = new StringBuilder();
		Matcher mat = s_namePat.matcher(bsh_code);
		String name = "Unnamed AST finder";
		if (mat.find())
		{
			name = mat.group(1).trim();
		}
		String head = new String(FileUtils.toBytes(AstAssertionFinder.class.getResourceAsStream("top.bsh")));
		head = head.replace("$NAME$", name);
		code.append(head);
		code.append(bsh_code);

		code.append(new String(FileUtils.toBytes(AstAssertionFinder.class.getResourceAsStream("bottom.bsh"))));
		Object o = interpreter.eval(code.toString());
		if (o == null || !(o instanceof AstAssertionFinderFactory))
		{
			return null;
		}
		return (AstAssertionFinderFactory) o;
	}

	/**
	 * Sets up the command line interface parser with the appropriate options.
	 * 
	 * @return The command line parser
	 */
	protected static CliParser setupCli()
	{
		CliParser cli = new CliParser();
		cli.addArgument(new Argument().withShortName("o").withLongName("output")
				.withDescription("Output file (default: report.html)").withArgument("file"));
		cli.addArgument(new Argument().withShortName("s").withLongName("source")
				.withDescription("Additional source in path").withArgument("path"));
		cli.addArgument(new Argument().withShortName("j").withLongName("jar")
				.withDescription("Additional jar file(s) in path").withArgument("path"));
		cli.addArgument(new Argument().withShortName("t").withLongName("threads").withArgument("n")
				.withDescription("Use up to n threads"));
		cli.addArgument(new Argument().withShortName("q").withLongName("quiet")
				.withDescription("Do not show error messages"));
		cli.addArgument(new Argument().withShortName("m").withLongName("summary")
				.withDescription("Only show a summary at the CLI"));
		cli.addArgument(new Argument().withShortName("c").withLongName("no-color")
				.withDescription("Disable colored output"));
		cli.addArgument(new Argument().withShortName("l").withLongName("limit").withArgument("n")
				.withDescription("Stop after n files (for testing purposes)"));
		cli.addArgument(new Argument().withShortName("h").withLongName("help")
				.withDescription("Display this help message"));
		cli.addArgument(new Argument().withShortName("p").withLongName("profile").withArgument("file")
				.withDescription("Get options from file"));
		cli.addArgument(new Argument().withShortName("u").withLongName("unresolved")
				.withDescription("Show unresolved symbols"));
		cli.addArgument(new Argument().withShortName("r").withLongName("root").withArgument("p")
				.withDescription("Search in source tree for package p"));
		cli.addArgument(new Argument().withShortName("l").withLongName("sample").withArgument("p")
				.withDescription("Sample code snippets with probability p"));
		cli.addArgument(new Argument().withShortName("d").withLongName("resolution-timeout").withArgument("ms")
				.withDescription("Set timeout for type resolution operations (in ms, default: 100)"));
		cli.addArgument(new Argument().withShortName("y").withLongName("query").withArgument("x")
				.withDescription("Read queries from x (file or folder)"));
		return cli;
	}

	/**
	 * Displays usage information for the command line interface.
	 * 
	 * @param out
	 *          The output stream to which the usage information is sent
	 * @param cli
	 *          The command line parser
	 */
	protected static void showUsage(CliParser cli)
	{
		cli.printHelp("", s_stdout);
	}

	protected static void processBatch(ExecutorService e, FileProvider provider,
			Set<AstAssertionFinderFactory> ast_finders, Set<SparqlTokenFinderFactory> sparql_finders, Set<FoundToken> found, boolean quiet, StatusCallback status,
			int limit) throws IOException, FileSystemException
	{
		int count = 0;
		Set<AssertionFinderRunnable> tasks = new HashSet<>();
		List<Future<?>> futures = new ArrayList<>();
		while (provider.hasNext() && (limit == -1 || count < limit))
		{
			count++;
			FileSource f_source = provider.next();
			{
				AstAssertionFinderRunnable r = new AstAssertionFinderRunnable(f_source, ast_finders, quiet, status);
				tasks.add(r);
				futures.add(e.submit(r));
			}
			{
				SparqlAssertionFinderRunnable r = new SparqlAssertionFinderRunnable(f_source, sparql_finders, quiet, status);
				tasks.add(r);
				futures.add(e.submit(r));
			}
		}
		waitForEnd(futures);
		e.shutdown(); // All tasks are finished, shutdown the executor
		for (AssertionFinderRunnable r : tasks)
		{
			found.addAll(r.getFound());
		}
	}

	public static void waitForEnd(List<Future<?>> futures)
	{
		for (Future<?> f : futures)
		{
			try
			{
				f.get(); // blocks until this task completes; rethrows exceptions from the task
			}
			catch (InterruptedException ie)
			{
				Thread.currentThread().interrupt();
				// If interrupted, you can choose to cancel outstanding tasks:
				for (Future<?> other : futures)
					other.cancel(true);
				throw new RuntimeException("Interrupted while waiting for tasks", ie);
			}
			catch (ExecutionException ee)
			{
				// The task threw; unwrap and either log or fail fast
				throw new RuntimeException("Task failed", ee.getCause());
			}
		}

	}

	protected static void categorize(Map<String, List<FoundToken>> map, Set<FoundToken> found)
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



	protected static class EndRunnable implements Runnable
	{
		private final Map<String, List<FoundToken>> m_found;

		private final boolean m_summary;

		public EndRunnable(Map<String, List<FoundToken>> found, boolean summary)
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
				cli_reporter.report(null, m_found, new HashSet<String>());
			}
			catch (IOException e)
			{
				// Ignore
			}
		}
	}

	/**
	 * Formats a duration in milliseconds into a human-readable string.
	 * @param duration The duration in milliseconds
	 * @return A human-readable string representing the duration
	 */
	public static String formatDuration(long duration)
	{
		if (duration < 1000)
		{
			return duration + " ms";
		}
		else if (duration < 60000)
		{
			return (duration / 1000) + " s";
		}
		else
		{
			long minutes = duration / 60000;
			long seconds = (duration % 60000) / 1000;
			return minutes + " min " + seconds + " s";
		}
	}

	/**
	 * Formats a duration in milliseconds into a string of the form HH:MM:SS.
	 * @param duration The duration in milliseconds
	 * @return A string of the form HH:MM:SS
	 */
	public static String formatHms(long duration)
	{
		long hours = duration / 3600000;
		long minutes = (duration % 3600000) / 60000;
		long seconds = (duration % 60000) / 1000;
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}
}
