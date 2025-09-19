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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import ca.uqac.lif.codefinder.find.FoundToken;
import ca.uqac.lif.codefinder.find.TokenFinderFactory;
import ca.uqac.lif.codefinder.find.TokenFinderCallable;
import ca.uqac.lif.codefinder.find.TokenFinderFactory.TokenFinderFactoryException;
import ca.uqac.lif.codefinder.find.sparql.SparqlTokenFinderFactory;
import ca.uqac.lif.codefinder.find.sparql.SparqlTokenFinderCallable;
import ca.uqac.lif.codefinder.find.visitor.VisitorAssertionFinderFactory;
import ca.uqac.lif.codefinder.find.visitor.VisitorAssertionFinderCallable;
import ca.uqac.lif.codefinder.provider.FileProvider;
import ca.uqac.lif.codefinder.provider.FileSource;
import ca.uqac.lif.codefinder.util.AnsiPrinter;
import ca.uqac.lif.codefinder.util.StatusCallback;
import ca.uqac.lif.fs.FilePath;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.FileUtils;
import ca.uqac.lif.fs.HardDisk;
import ca.uqac.lif.util.CliParser;
import ca.uqac.lif.util.CliParser.Argument;
import ca.uqac.lif.util.CliParser.ArgumentMap;

/**
 * An analysis of a Java project, as configured through command line
 * arguments.
 */
public class Analysis
{
	private Analysis()
	{
		// Prevent instantiation from outside the static methods
	}
	
	/**
	 * Reads command line arguments and returns an analysis object
	 * 
	 * @param cli    The command line parser
	 * @param map    The argument map
	 * @param stdout Printer for standard output
	 * @param stderr Printer for error output
	 * @return The analysis object
	 * @throws AnalysisCliException If an error occurs while reading the arguments
	 */
	public static Analysis read(CliParser cli, ArgumentMap map, AnsiPrinter stdout, AnsiPrinter stderr) throws AnalysisCliException
	{
		Analysis a = new Analysis();
		a.setStdout(stdout);
		a.setStderr(stderr);
		read(cli, map, a);
		return a;
	}

	/**
	 * Sets up the command line interface parser with the appropriate options.
	 * 
	 * @return The command line parser
	 */
	public static CliParser setupCli()
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
		cli.addArgument(
				new Argument().withShortName("d").withLongName("resolution-timeout").withArgument("ms")
				.withDescription("Set timeout for type resolution operations (in ms, default: 100)"));
		cli.addArgument(new Argument().withShortName("y").withLongName("query").withArgument("x")
				.withDescription("Read queries from x (file or folder)"));
		cli.addArgument(new Argument().withShortName("f").withLongName("follow").withArgument("d")
				.withDescription("Follow method calls up to depth d (default: 0)"));
		cli.addArgument(new Argument().withLongName("no-cache")
				.withDescription("Do not reuse cached analysis results"));
		cli.addArgument(new Argument().withLongName("project").withArgument("name")
				.withDescription("Set the project name (used for caching)"));
		return cli;
	}


	protected static void read(CliParser cli, ArgumentMap map, Analysis a) throws AnalysisCliException
	{
		if (map == null)
		{
			throw new AnalysisCliException("Error in command line arguments");
		}
		a.m_others.addAll(map.getOthers());
		if (map.containsKey("profile"))
		{
			readProfile(cli, a, map.getOptionValue("profile"));
		}
		a.setCache(!map.containsKey("no-cache"));
		if (map.containsKey("project"))
		{
			a.setProjectName(map.getOptionValue("project"));
		}
		else if (a.getProjectName().isEmpty())
		{
			a.getStdout().println("Project name not specified, using '' (use --project to specify)");
		}
		if (map.containsKey("query"))
		{
			fetchQueries(a, map);
		}
		if (map.containsKey("resolution-timeout"))
		{
			try
			{
				a.setResolutionTimeout(Long.parseLong(map.getOptionValue("resolution-timeout").trim()));
			}
			catch (NumberFormatException e)
			{
				throw new AnalysisCliException(e);
			}
		}
		if (map.containsKey("no-color"))
		{
			a.getStdout().disableColors();
			a.getStderr().disableColors();
		}
		a.setQuiet(map.containsKey("quiet"));
		a.setUnresolved(map.containsKey("unresolved"));
		a.setSummary(!map.containsKey("summary"));
		if (map.containsKey("output"))
		{
			a.setOutputFile(map.getOptionValue("output"));
		}
		if (map.containsKey("follow"))
		{
			a.setFollow(Integer.parseInt(map.getOptionValue("output").trim()));
		}
		if (map.containsKey("source"))
		{
			String[] paths = map.getOptionValue("source").split(":");
			for (String p : paths)
			{
				a.getSourcePaths().add(p);
			}
		}
		if (map.containsKey("jar"))
		{
			String[] paths = map.getOptionValue("jar").split(":");
			for (String p : paths)
			{
				a.getJarPaths().add(p);
			}
		}
		if (map.hasOption("threads"))
		{
			a.setThreads(Integer.parseInt(map.getOptionValue("threads").trim()));
		}
		if (map.hasOption("limit"))
		{
			a.setLimit(Integer.parseInt(map.getOptionValue("limit").trim()));
			a.getStdout().println("Analysis limited to first " + a.getLimit() + " files");
		}
		if (map.hasOption("root"))
		{
			a.setRoot(map.getOptionValue("root"));
		}
		if (map.containsKey("help"))
		{
			a.showUsage(cli);
		}
	}

	/**
	 * The name of the project being analyzed
	 */
	protected String m_projectName;

	/**
	 * Additional source paths
	 */
	protected Set<String> m_sourcePaths = new HashSet<>();

	/**
	 * Additional jar files
	 */
	protected Set<String> m_jarPaths = new HashSet<>();

	/**
	 * A callback to send notifications about the analysis' status.
	 */
	protected StatusCallback m_callback;

	/**
	 * Name of the output file
	 */
	protected String m_outputFile;

	/**
	 * Whether to operate in quiet mode (no error messages)
	 */
	protected boolean m_quiet = false;

	/**
	 * The name of the root package to look for in the source tree
	 */
	protected String m_root = null;

	/**
	 * Whether to show unresolved symbols.
	 */
	protected boolean m_unresolved = false;

	/**
	 * Number of threads to use. If not specified, use number of processors - 1.
	 */
	protected int m_threads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);

	/**
	 * Whether to only show a summary at the command line
	 */
	protected static boolean m_summary = false;

	/**
	 * Limit to the number of files to process (for testing purposes). A negative
	 * value means no limit.
	 */
	protected int m_limit = -1;

	/**
	 * Depth to which method calls should be followed
	 */
	protected int m_follow = 0;

	/**
	 * Timeout for type resolution operations (in milliseconds)
	 */
	protected long m_resolutionTimeout = 100;
	
	/**
	 * Other command line arguments (not parsed)
	 */
	protected final List<String> m_others = new ArrayList<>();
	
	/**
	 * The path in which the executable is executed
	 */
	protected final FilePath m_homePath = new FilePath(System.getProperty("user.dir"));


	/**
	 * Whether to cache analysis results
	 */
	protected boolean m_cache = true;

	/**
	 * The name of the folder to use for caching
	 */
	protected String m_cacheFolder = ".codefinder_cache";

	/**
	 * The set of assertion finders working on the AST
	 */
	protected Set<VisitorAssertionFinderFactory> m_astFinders = new HashSet<>();

	/**
	 * The set of assertion finders working through SPARQL queries
	 */
	protected Set<SparqlTokenFinderFactory> m_sparqlFinders = new HashSet<>();

	/**
	 * The set of assertion finders with cached results
	 */
	protected Set<TokenFinderFactory> m_cachedFinders = new HashSet<>();

	/**
	 * Printer for standard output
	 */
	protected AnsiPrinter m_stdout = null;

	/**
	 * Printer for error output
	 */
	protected AnsiPrinter m_stderr = null;

	public void setStdout(AnsiPrinter stdout)
	{
		m_stdout = stdout;
	}

	public void setStderr(AnsiPrinter stderr)
	{
		m_stderr = stderr;
	}

	public AnsiPrinter getStdout()
	{
		return m_stdout;
	}

	public AnsiPrinter getStderr()
	{
		return m_stderr;
	}

	public String getProjectName()
	{
		return m_projectName;
	}

	public void setProjectName(String projectName)
	{
		this.m_projectName = projectName;
	}

	public void setUnresolved(boolean unresolved)
	{
		this.m_unresolved = unresolved;
	}

	public boolean getUnresolved()
	{
		return m_unresolved;
	}

	public Set<String> getSourcePaths()
	{
		return m_sourcePaths;
	}

	public void setSourcePaths(Set<String> sourcePaths)
	{
		this.m_sourcePaths = sourcePaths;
	}

	public Set<String> getJarPaths()
	{
		return m_jarPaths;
	}

	public void setJarPaths(Set<String> jarPaths)
	{
		this.m_jarPaths = jarPaths;
	}

	public StatusCallback getCallback()
	{
		return m_callback;
	}

	public void setCallback(StatusCallback callback)
	{
		this.m_callback = callback;
	}

	public FilePath getHomePath()
	{
		return m_homePath;
	}

	public String getOutputFile()
	{
		return m_outputFile;
	}

	public void setOutputFile(String outputFile)
	{
		this.m_outputFile = outputFile;
	}

	public boolean isQuiet()
	{
		return m_quiet;
	}

	public void setQuiet(boolean quiet)
	{
		this.m_quiet = quiet;
	}

	public String getRoot()
	{
		return m_root;
	}

	public void setRoot(String root)
	{
		this.m_root = root;
	}

	public int getThreads()
	{
		return m_threads;
	}

	public void setThreads(int threads)
	{
		this.m_threads = threads;
	}

	public boolean getSummary()
	{
		return m_summary;
	}

	public void setSummary(boolean summary)
	{
		m_summary = summary;
	}

	public int getLimit()
	{
		return m_limit;
	}

	public void setLimit(int limit)
	{
		this.m_limit = limit;
	}

	public int getFollow()
	{
		return m_follow;
	}

	public void setFollow(int follow)
	{
		this.m_follow = follow;
	}

	public long getResolutionTimeout()
	{
		return m_resolutionTimeout;
	}

	public void setResolutionTimeout(long resolutionTimeout)
	{
		this.m_resolutionTimeout = resolutionTimeout;
	}

	public boolean isCache()
	{
		return m_cache;
	}

	public void setCache(boolean cache)
	{
		this.m_cache = cache;
	}

	public String getCacheFolder()
	{
		return m_cacheFolder;
	}

	public void setCacheFolder(String cacheFolder)
	{
		this.m_cacheFolder = cacheFolder;
	}

	public Set<VisitorAssertionFinderFactory> getAstFinders()
	{
		return m_astFinders;
	}

	public void setAstFinders(Set<VisitorAssertionFinderFactory> astFinders)
	{
		this.m_astFinders = astFinders;
	}

	public Set<SparqlTokenFinderFactory> getSparqlFinders()
	{
		return m_sparqlFinders;
	}

	public void setSparqlFinders(Set<SparqlTokenFinderFactory> sparqlFinders)
	{
		this.m_sparqlFinders = sparqlFinders;
	}

	public Set<TokenFinderFactory> getCachedFinders()
	{
		return m_cachedFinders;
	}

	public void setCachedFinders(Set<TokenFinderFactory> cachedFinders)
	{
		this.m_cachedFinders = cachedFinders;
	}

	protected static void readProfile(CliParser cli, Analysis a, String filename) throws AnalysisCliException
	{
		FilePath output_path = a.getHomePath().chdir(getPathOfFile(filename));
		FilePath reverse_path = output_path.chdir(getPathOfFile(filename));
		try
		{
			HardDisk hd = new HardDisk(reverse_path.toString()).open();
			String contents = readLinesWithComments(hd.readFrom(getFilename(filename)));
			hd.close();
			String[] args = contents.toString().split(" ");
			ArgumentMap map = cli.parse(args);
			read(cli, map, a);
		}
		catch (FileSystemException e)
		{
			throw new AnalysisCliException(e);
		}
	}

	/**
	 * Returns other command line arguments (not parsed)
	 * @return The other arguments
	 */
	public List<String> getOthers()
	{
		return m_others;
	}

	public List<Future<Set<FoundToken>>> processBatch(ExecutorService e, FileProvider provider,
			Set<FoundToken> found) throws IOException, FileSystemException, TokenFinderFactoryException
	{
		checkCachedFinders();
		for (TokenFinderFactory f : m_cachedFinders)
		{
			found.addAll(f.readCache(new HardDisk(m_cacheFolder), m_projectName));
		}
		int count = 0;
		Set<TokenFinderCallable> tasks = new HashSet<>();
		List<Future<Set<FoundToken>>> futures = new ArrayList<>();
		while (provider.hasNext() && (m_limit == -1 || count < m_limit))
		{
			count++;
			FileSource f_source = provider.next();
			if (!m_astFinders.isEmpty())
			{
				VisitorAssertionFinderCallable r = new VisitorAssertionFinderCallable(m_projectName, f_source,
						m_astFinders, m_quiet, m_callback);
				tasks.add(r);
				futures.add(e.submit(r));
			}
			if (!m_sparqlFinders.isEmpty())
			{
				SparqlTokenFinderCallable r = new SparqlTokenFinderCallable(m_projectName, f_source,
						m_sparqlFinders, m_quiet, m_callback, m_follow);
				tasks.add(r);
				futures.add(e.submit(r));
			}
		}
		return futures;
	}

	protected void checkCachedFinders() throws FileSystemException, TokenFinderFactoryException
	{
		// Check which finders have cached results
		if (m_cache && !m_projectName.isEmpty())
		{
			HardDisk hd = new HardDisk().open();
			if (!hd.isDirectory(m_cacheFolder))
			{
				hd.mkdir(m_cacheFolder);
			}
			hd.pushd(m_cacheFolder);
			{
				Iterator<VisitorAssertionFinderFactory> it = m_astFinders.iterator();
				while (it.hasNext())
				{
					VisitorAssertionFinderFactory f = it.next();
					if (f.isCached(hd, m_projectName))
					{
						m_cachedFinders.add(f);
						it.remove();
					}
				}
			}
			{
				Iterator<SparqlTokenFinderFactory> it = m_sparqlFinders.iterator();
				while (it.hasNext())
				{
					SparqlTokenFinderFactory f = it.next();
					if (f.isCached(hd, m_projectName))
					{
						m_cachedFinders.add(f);
						it.remove();
					}
				}
			}
			hd.close();
		}
	}

	protected void showUsage(CliParser cli)
	{
		cli.printHelp("", m_stdout);
	}

	protected static void fetchQueries(Analysis a, ArgumentMap map) throws AnalysisCliException
	{
		a.getAstFinders().clear(); // Override whatever the profile says
		a.getSparqlFinders().clear(); // Override whatever the profile says
		String bsh_file = map.getOptionValue("query");
		try
		{
			HardDisk hd = new HardDisk(a.getHomePath().toString()).open();
			if (hd.isDirectory(bsh_file))
			{
				a.getStdout().println("Reading queries from folder " + bsh_file);
				hd.pushd(bsh_file);
				{
					// First the BeanShell scripts
					List<String> files = FileUtils.ls(hd, "", "^.*bsh$");
					for (String f : files)
					{
						try
						{
							VisitorAssertionFinderFactory factory = VisitorAssertionFinderFactory.readBeanshell(hd, bsh_file + "/" + f);
							a.getAstFinders().add(factory);
						}
						catch (TokenFinderFactoryException e)
						{
							throw new AnalysisCliException(e);
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
							SparqlTokenFinderFactory factory = SparqlTokenFinderFactory.readSparql(hd, bsh_file + "/" + f);
							a.getSparqlFinders().add(factory);
						}
						catch (TokenFinderFactoryException e)
						{
							throw new AnalysisCliException(e);
						}
					}
				}
				hd.popd();
			}
			else
			{
				if (bsh_file.endsWith(".sparql"))
				{
					a.getStdout().println("Reading SPARQL query from file " + bsh_file);
					hd.pushd(getPathOfFile(bsh_file).toString());
					SparqlTokenFinderFactory factory = SparqlTokenFinderFactory.readSparql(hd, bsh_file);
					hd.popd();
					a.getSparqlFinders().add(factory);
				}
				else if (bsh_file.endsWith(".bsh"))
				{
					a.getStdout().println("Reading BeanShell script from file " + bsh_file);
					hd.pushd(getPathOfFile(bsh_file).toString());
					VisitorAssertionFinderFactory factory = VisitorAssertionFinderFactory.readBeanshell(hd, bsh_file);
					hd.popd();
					a.getAstFinders().add(factory);
				}
			}
		}
		catch (TokenFinderFactoryException e)
		{
			throw new AnalysisCliException(e);
		}
		catch (FileSystemException e)
		{
			throw new AnalysisCliException(e);
		}
	}

	protected static String readLinesWithComments(InputStream is)
	{
		StringBuilder contents = new StringBuilder();
		Scanner scanner = new Scanner(is);
		while (scanner.hasNextLine())
		{
			String line = scanner.nextLine().trim();
			if (line.isEmpty() || line.startsWith("#"))
				continue;
			contents.append(line).append(" ");
		}
		scanner.close();
		return contents.toString();
	}

	public static class AnalysisCliException extends Throwable
	{
		private static final long serialVersionUID = 1L;

		public AnalysisCliException(String message)
		{
			super(message);
		}

		public AnalysisCliException(Throwable cause)
		{
			super(cause);
		}
	}
}