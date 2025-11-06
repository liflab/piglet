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
package ca.uqac.lif.piglet;

import static ca.uqac.lif.piglet.util.Paths.getFilename;
import static ca.uqac.lif.piglet.util.Paths.getPathOfFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Future;

import ca.uqac.lif.fs.FilePath;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.FileUtils;
import ca.uqac.lif.fs.HardDisk;
import ca.uqac.lif.piglet.find.FactoryCache;
import ca.uqac.lif.piglet.find.FileFilter;
import ca.uqac.lif.piglet.find.FoundToken;
import ca.uqac.lif.piglet.find.PassthroughFileFilter;
import ca.uqac.lif.piglet.find.SubstringFileFilter;
import ca.uqac.lif.piglet.find.TokenFinderCallable;
import ca.uqac.lif.piglet.find.TokenFinderFactory;
import ca.uqac.lif.piglet.find.TokenFinderFactory.TokenFinderFactoryException;
import ca.uqac.lif.piglet.find.sparql.SparqlTokenFinderCallable;
import ca.uqac.lif.piglet.find.sparql.SparqlTokenFinderFactory;
import ca.uqac.lif.piglet.find.visitor.VisitorAssertionFinderCallable;
import ca.uqac.lif.piglet.find.visitor.VisitorAssertionFinderFactory;
import ca.uqac.lif.piglet.provider.FileProvider;
import ca.uqac.lif.piglet.provider.FileSource;
import ca.uqac.lif.piglet.util.Paths;
import ca.uqac.lif.piglet.util.StatusCallback;
import ca.uqac.lif.util.AnsiPrinter;
import ca.uqac.lif.util.CliParser;
import ca.uqac.lif.util.CliParser.Argument;
import ca.uqac.lif.util.CliParser.ArgumentMap;

/**
 * An analysis of a Java project, as configured through command line
 * arguments.
 */
public class Analysis implements Comparable<Analysis>
{
	private Analysis()
	{
		// Prevent instantiation from outside the static methods
		super();
	}

	/**
	 * Compare this analysis to another by project name.
	 *
	 * @param o the other Analysis object to compare with
	 * @return a negative integer, zero, or a positive integer as this
	 *         object's project name is less than, equal to, or greater than
	 *         the specified object's project name
	 */
	@Override
	public int compareTo(Analysis o)
	{
		return this.getProjectName().compareTo(o.getProjectName());
	}

	/**
	 * A set of file patterns to ignore
	 */
	protected Set<String> m_ignoredFiles = new HashSet<>();

	/**
	 * Reads command line arguments and returns a set of analysis objects
	 * @param analyses The set to populate with analysis objects
	 * @param cli    The command line parser
	 * @param map    The argument map
	 * @param stdout Printer for standard output
	 * @param stderr Printer for error output
	 * @throws AnalysisCliException If an error occurs while reading the arguments
	 */
	public static void read(Set<Analysis> analyses, CliParser cli, ArgumentMap map, AnsiPrinter stdout, AnsiPrinter stderr) throws AnalysisCliException
	{
		for (String profile : map.getOthers())
		{
			Analysis a = new Analysis();
			a.setStdout(stdout);
			a.setStderr(stderr);
			readProfile(cli, a, profile);
			read(cli, map, a);
			analyses.add(a);
		}
	}

	/**
	 * Sets up the command line interface parser with the appropriate options.
	 * 
	 * @return The command line parser
	 */
	public static CliParser setupCli()
	{
		CliParser cli = new CliParser();
		{
			Argument arg = new Argument().withShortName("o").withLongName("output")
				.withDescription("Output file (default: report.html)").withArgument("file");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withShortName("s").withLongName("source")
				.withDescription("Additional source in path").withArgument("path");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withShortName("j").withLongName("jar")
				.withDescription("Additional jar file(s) in path").withArgument("path");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withShortName("t").withLongName("threads").withArgument("n")
				.withDescription("Use up to n threads");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withShortName("q").withLongName("quiet")
				.withDescription("Do not show error messages");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withShortName("m").withLongName("summary")
				.withDescription("Only show a summary at the CLI");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withShortName("c").withLongName("no-color")
				.withDescription("Disable colored output");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withShortName("l").withLongName("limit").withArgument("n")
				.withDescription("Stop after n files (for testing purposes)");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withShortName("h").withLongName("help")
				.withDescription("Display this help message");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withShortName("u").withLongName("unresolved")
				.withDescription("Show unresolved symbols");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withShortName("r").withLongName("root").withArgument("p")
				.withDescription("Search in source tree for package p");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withShortName("l").withLongName("sample").withArgument("p")
				.withDescription("Sample code snippets with probability p");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withShortName("d").withLongName("resolution-timeout").withArgument("ms")
				.withDescription("Set timeout for type resolution operations (in ms, default: 100)");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withShortName("y").withLongName("query").withArgument("x")
				.withDescription("Read queries from x (file or folder)");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withShortName("f").withLongName("follow").withArgument("d")
				.withDescription("Follow method calls up to depth d (default: 0)");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withLongName("no-cache")
				.withDescription("Do not reuse cached analysis results");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withShortName("p").withLongName("project").withArgument("name")
				.withDescription("Set the project name (used for caching)");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withShortName("f").withLongName("force-cache")
				.withDescription("Skips the cache integrity check (use with care!)");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withShortName("i").withLongName("ignore").withArgument("filespec")
				.withDescription("Ignore files");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withShortName("1").withLongName("halt-on-first")
				.withDescription("Halt on first match");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withLongName("timeout").withArgument("s")
				.withDescription("Set timeout for individual file analysis (in s, default: 15)");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withShortName("T").withLongName("global-timeout").withArgument("s")
				.withDescription("Set timeout for global file analysis (in s, default: -1 (no timeout))");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withShortName("R").withLongName("rdf").withArgument("file")
				.withDescription("Export file to RDF");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withShortName("P").withLongName("printout")
				.withDescription("Minimal printout of progress to the console");
			cli.addArgument(arg);
		}
		{
			Argument arg = new Argument().withShortName("F").withLongName("filteron").withArgument("substring")
				.withDescription("Only analyze files whose code contains the given substring");
			cli.addArgument(arg);
		}
		return cli;
	}


	/**
	 * Reads command line arguments into the provided Analysis object.
	 * This method sets fields on the given Analysis instance according to
	 * the options present in the parsed argument map.
	 *
	 * @param cli the command line parser used to display help and parse profiles
	 * @param map the parsed argument map containing options and values
	 * @param a   the Analysis object to populate
	 * @throws AnalysisCliException if an error occurs while reading or parsing
	 *                              numerical options
	 */
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
		if (map.containsKey("halt-on-first"))
		{
			a.m_haltOnFirst = true;
		}
		a.setCache(!map.containsKey("no-cache"));
		if (map.containsKey("force-cache"))
		{
			a.m_forceCache = true;
		}
		if (map.containsKey("printout"))
		{
			a.m_printout = true;
		}
		if (map.containsKey("project"))
		{
			a.setProjectName(map.getOptionValue("project"));
		}
		else if (a.getProjectName().isEmpty())
		{
			a.getStdout().println("Project name not specified, using '' (use --project to specify)");
		}
		if (map.containsKey("ignore"))
		{
			String[] patterns = map.getOptionValue("ignore").split(":");
			for (String p : patterns)
			{
				a.m_ignoredFiles.add(p);
			}
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
		if (map.containsKey("timeout"))
		{
			try
			{
				long to = Long.parseLong(map.getOptionValue("timeout").trim());
				a.m_fileTimeout = to;
			}
			catch (NumberFormatException e)
			{
				throw new AnalysisCliException(e);
			}
		}
		if (map.containsKey("global-timeout"))
		{
			try
			{
				long to = Long.parseLong(map.getOptionValue("global-timeout").trim());
			 a.m_globalTimeout = to;
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
		if (map.containsKey("filteron"))
		{
			a.m_filterSubstring = map.getOptionValue("filteron");
		}
		if (map.containsKey("quiet"))
		{
			a.setQuiet(true);
		}
		if (map.containsKey("unresolved"))
		{
			a.setUnresolved(true);
		}
		if (map.containsKey("summary"))
		{
			a.setSummary(true);
		}
		if (map.containsKey("no-cache"))
		{
			a.setCache(false);
		}
		if (map.containsKey("output"))
		{
			a.setOutputFile(map.getOptionValue("output"));
		}
		if (map.containsKey("follow"))
		{
		 a.setFollow(Integer.parseInt(map.getOptionValue("follow").trim()));
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
			String[] roots = map.getOptionValue("root").split(":");
			a.setRoots(roots);
		}
		if (map.containsKey("help"))
		{
			a.showUsage(cli);
		}
	}

	/**
	 * Returns the repository path associated with this analysis.
	 *
	 * @return the repository path as a String (may be empty)
	 */
	public String getRepositoryPath()
	{
		return m_repositoryPath;
	}

	/**
	 * Returns the path of the report file where results will be written.
	 * If no output file has been set, a default name is returned.
	 *
	 * @return the report file path
	 */
	public String getReportPath()
	{
		if (m_outputFile == null || m_outputFile.isEmpty())
		{
			return "report.html";
		}
		return m_outputFile;
	}

	/**
	 * The name of the project being analyzed
	 */
	protected String m_projectName;

	/**
	 * Additional source paths
	 */
	protected List<String> m_sourcePaths = new ArrayList<>();

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
	 * The name(s) of the root package(s) to look for in the source tree
	 */
	protected String[] m_root = null;

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
	protected boolean m_summary = false;

	/**
	 * Limit to the number of files to process (for testing purposes). A negative
	 * value means no limit.
	 */
	protected int m_limit = -1;

	/**
	 * Path to the repository (if any)
	 */
	protected String m_repositoryPath = "";

	/**
	 * Depth to which method calls should be followed
	 */
	protected int m_follow = 0;
	
	/**
	 * Whether to use a minimal printout of progress to the console
	 * (default: false).
	 */
	protected boolean m_printout = false;

	/**
	 * Timeout for type resolution operations (in milliseconds)
	 */
	protected long m_resolutionTimeout = 100;
	
	/**
	 * Global timeout for the analysis (in milliseconds). A negative value means no
	 * timeout.
	 */
	protected long m_fileTimeout = 15000;
	
	/**
	 * Timeout for individual file analysis (in milliseconds). A negative value
	 * means no timeout.
	 */
	protected long m_globalTimeout = -1;

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
	 * Whether to halt on first match.
	 */
	protected boolean m_haltOnFirst = false;

	/**
	 * The name of the folder to use for caching
	 */
	protected String m_cacheFolder = ".cache";

	/**
	 * The set of assertion finders working on the AST using
	 * the visitor pattern
	 */
	protected Set<VisitorAssertionFinderFactory> m_visitorFinders = new HashSet<>();

	/**
	 * The set of assertion finders working through SPARQL queries
	 */
	protected Set<SparqlTokenFinderFactory> m_sparqlFinders = new HashSet<>();

	/**
	 * The set of assertion finders with cached results
	 */
	protected Set<TokenFinderFactory> m_cachedFinders = new HashSet<>();
	
	/**
	 * A substring to filter files on. If non-null, only files whose code
	 * contains this substring will be analyzed.
	 */
	protected String m_filterSubstring = null;
	
	/**
	 * The set of caches 
	 */
	protected Set<FactoryCache> m_caches = new HashSet<>();
	
	/**
	 * Whether to skip the cache integrity check (use with care!)
	 */
	protected boolean m_forceCache = false;

	/**
	 * Printer for standard output
	 */
	protected AnsiPrinter m_stdout = null;

	/**
	 * Printer for error output
	 */
	protected AnsiPrinter m_stderr = null;

	protected Map<Future<TokenFinderCallable.CallableFuture>,String> m_futureToFile = new IdentityHashMap<>();

	/**
	 * Sets the repository path used by the analysis.
	 *
	 * @param path the repository path to set
	 */
	public void setRepositoryPath(String path)
	{
		m_repositoryPath = path;
	}

	/**
	 * Sets the printer used for standard output messages.
	 *
	 * @param stdout an AnsiPrinter to use for standard output
	 */
	public void setStdout(AnsiPrinter stdout)
	{
		m_stdout = stdout;
	}

	/**
	 * Sets the printer used for error messages.
	 *
	 * @param stderr an AnsiPrinter to use for error output
	 */
	public void setStderr(AnsiPrinter stderr)
	{
		m_stderr = stderr;
	}

	/**
	 * Gets the printer used for standard output.
	 *
	 * @return the AnsiPrinter used for standard output (may be null)
	 */
	public AnsiPrinter getStdout()
	{
		return m_stdout;
	}

	/**
	 * Gets the printer used for error output.
	 *
	 * @return the AnsiPrinter used for error output (may be null)
	 */
	public AnsiPrinter getStderr()
	{
		return m_stderr;
	}

	/**
	 * Returns the project name associated with this analysis.
	 *
	 * @return the project name (may be null)
	 */
	public String getProjectName()
	{
		return m_projectName;
	}

	/**
	 * Sets the project name for this analysis (used for caching and display).
	 *
	 * @param projectName the project name to set
	 */
	public void setProjectName(String projectName)
	{
		this.m_projectName = projectName;
	}

	/**
	 * Enable or disable display of unresolved symbols.
	 *
	 * @param unresolved true to show unresolved symbols, false otherwise
	 */
	public void setUnresolved(boolean unresolved)
	{
		this.m_unresolved = unresolved;
	}

	/**
	 * Returns whether unresolved symbols are shown.
	 *
	 * @return true if unresolved symbols are shown, false otherwise
	 */
	public boolean getUnresolved()
	{
		return m_unresolved;
	}

	/**
	 * Returns the additional source paths configured for the analysis.
	 *
	 * @return a list of source path strings
	 */
	public List<String> getSourcePaths()
	{
		return m_sourcePaths;
	}

	/**
	 * Sets the additional source paths for the analysis.
	 *
	 * @param sourcePaths a list of source path strings
	 */
	public void setSourcePaths(List<String> sourcePaths)
	{
		this.m_sourcePaths = sourcePaths;
	}

	/**
	 * Returns the set of additional jar paths configured for the analysis.
	 *
	 * @return a set of jar path strings
	 */
	public Set<String> getJarPaths()
	{
		return m_jarPaths;
	}

	/**
	 * Sets the jar paths to use for the analysis.
	 *
	 * @param jarPaths a set of jar path strings
	 */
	public void setJarPaths(Set<String> jarPaths)
	{
		this.m_jarPaths = jarPaths;
	}

	/**
	 * Returns the file name associated with a submitted future task.
	 *
	 * @param f the Future returned when submitting a TokenFinderCallable
	 * @return the filename mapped to that future, or null if none
	 */
	public String getFileForFuture(Future<TokenFinderCallable.CallableFuture> f)
	{
		return m_futureToFile.get(f);
	}

	/**
	 * Returns the status callback used to send notifications about the analysis.
	 *
	 * @return the StatusCallback instance (may be null)
	 */
	public StatusCallback getCallback()
	{
		return m_callback;
	}

	/**
	 * Sets the status callback used during the analysis.
	 *
	 * @param callback the StatusCallback to set
	 */
	public void setCallback(StatusCallback callback)
	{
		this.m_callback = callback;
	}

	/**
	 * Returns the home path (current working directory) for the analysis.
	 *
	 * @return the FilePath representing the home path
	 */
	public FilePath getHomePath()
	{
		return m_homePath;
	}

	/**
	 * Returns the configured output file name for the analysis.
	 *
	 * @return the output file name, or null if none set
	 */
	public String getOutputFile()
	{
		return m_outputFile;
	}

	/**
	 * Sets the output file name where analysis results will be written.
	 *
	 * @param outputFile the output file name to set
	 */
	public void setOutputFile(String outputFile)
	{
		this.m_outputFile = outputFile;
	}

	/**
	 * Returns whether the analysis is in quiet mode (no error messages).
	 *
	 * @return true if quiet mode is enabled, false otherwise
	 */
	public boolean isQuiet()
	{
		return m_quiet;
	}

	/**
	 * Enable or disable quiet mode for the analysis.
	 *
	 * @param quiet true to enable quiet mode, false to disable
	 */
	public void setQuiet(boolean quiet)
	{
		this.m_quiet = quiet;
	}

	/**
	 * Determines if a finder (by name) has cached results.
	 *
	 * @param name The name of the finder
	 * @return true if the finder has cached results, false otherwise
	 */
	public boolean isCached(String name)
	{
		return m_cachedFinders.stream().anyMatch(f -> f.getName().equals(name));
	}

	/**
	 * Returns the root package names configured for the analysis.
	 *
	 * @return an array of root package strings, or null if none
	 */
	public String[] getRoots()
	{
		return m_root;
	}

	/**
	 * Sets the root package names to search in the source tree.
	 *
	 * @param roots an array of root package strings
	 */
	public void setRoots(String[] roots)
	{
		m_root = roots;
	}

	/**
	 * Returns the number of threads configured for the analysis.
	 *
	 * @return the number of threads
	 */
	public int getThreads()
	{
		return m_threads;
	}

	/**
	 * Sets the maximum number of threads to use for the analysis.
	 *
	 * @param threads the number of threads to use
	 */
	public void setThreads(int threads)
	{
		this.m_threads = threads;
	}

	/**
	 * Returns whether only a summary is displayed at the command line.
	 *
	 * @return true if summary mode is enabled, false otherwise
	 */
	public boolean getSummary()
	{
		return m_summary;
	}

	/**
	 * Enable or disable summary-only CLI output.
	 *
	 * @param summary true to enable summary mode, false to disable
	 */
	public void setSummary(boolean summary)
	{
		m_summary = summary;
	}

	/**
	 * Returns the limit on the number of files to process (-1 means no limit).
	 *
	 * @return the file processing limit
	 */
	public int getLimit()
	{
		return m_limit;
	}

	/**
	 * Sets a limit on the number of files to process (for testing purposes).
	 *
	 * @param limit the maximum number of files to process, or -1 for no limit
	 */
	public void setLimit(int limit)
	{
		this.m_limit = limit;
	}

	/**
	 * Returns how deep method calls should be followed during analysis.
	 *
	 * @return the follow depth
	 */
	public int getFollow()
	{
		return m_follow;
	}

	/**
	 * Sets the depth to which method calls should be followed.
	 *
	 * @param follow the follow depth
	 */
	public void setFollow(int follow)
	{
		this.m_follow = follow;
	}

	/**
	 * Returns the timeout used for type resolution operations (ms).
	 *
	 * @return the resolution timeout in milliseconds
	 */
	public long getResolutionTimeout()
	{
		return m_resolutionTimeout;
	}

	/**
	 * Sets the timeout used for type resolution operations (ms).
	 *
	 * @param resolutionTimeout the resolution timeout in milliseconds
	 */
	public void setResolutionTimeout(long resolutionTimeout)
	{
		this.m_resolutionTimeout = resolutionTimeout;
	}

	/**
	 * Returns whether caching of analysis results is enabled.
	 *
	 * @return true if caching is enabled, false otherwise
	 */
	public boolean isCache()
	{
		return m_cache;
	}

	/**
	 * Enable or disable caching of analysis results.
	 *
	 * @param cache true to enable caching, false to disable
	 */
	public void setCache(boolean cache)
	{
		this.m_cache = cache;
	}

	/**
	 * Returns the folder name used for caching analysis results.
	 *
	 * @return the cache folder name
	 */
	public String getCacheFolder()
	{
		return m_cacheFolder;
	}

	/**
	 * Sets the folder name used to store cached analysis results.
	 *
	 * @param cacheFolder the cache folder name to set
	 */
	public void setCacheFolder(String cacheFolder)
	{
		this.m_cacheFolder = cacheFolder;
	}

	/**
	 * Returns the set of AST-based assertion finder factories configured.
	 *
	 * @return a set of VisitorAssertionFinderFactory instances
	 */
	public Set<VisitorAssertionFinderFactory> getAstFinders()
	{
		return m_visitorFinders;
	}
	
	/**
	 * Gets the file filter applied to files before analysis. If a substring
	 * filter is configured, returns a SubstringFileFilter; otherwise returns
	 * a PassthroughFileFilter.
	 *
	 * @return the FileFilter to use for the analysis
	 */
	public FileFilter getFilter()
	{
		if (m_filterSubstring != null)
		{
			return new SubstringFileFilter(m_filterSubstring);
		}
		return new PassthroughFileFilter();
	}

	/**
	 * Sets the AST-based assertion finder factories to use during analysis.
	 *
	 * @param astFinders the set of VisitorAssertionFinderFactory instances
	 */
	public void setAstFinders(Set<VisitorAssertionFinderFactory> astFinders)
	{
		this.m_visitorFinders = astFinders;
	}

	/**
	 * Returns the set of SPARQL-based assertion finder factories configured.
	 *
	 * @return a set of SparqlTokenFinderFactory instances
	 */
	public Set<SparqlTokenFinderFactory> getSparqlFinders()
	{
		return m_sparqlFinders;
	}

	/**
	 * Sets the SPARQL-based assertion finder factories to use.
	 *
	 * @param sparqlFinders the set of SparqlTokenFinderFactory instances
	 */
	public void setSparqlFinders(Set<SparqlTokenFinderFactory> sparqlFinders)
	{
		this.m_sparqlFinders = sparqlFinders;
	}

	/**
	 * Returns the set of token finder factories that had cached results.
	 *
	 * @return a set of TokenFinderFactory instances with cached results
	 */
	public Set<TokenFinderFactory> getCachedFinders()
	{
		return m_cachedFinders;
	}

	/**
	 * Sets the set of token finder factories that had cached results.
	 *
	 * @param cachedFinders the set of TokenFinderFactory instances
	 */
	public void setCachedFinders(Set<TokenFinderFactory> cachedFinders)
	{
		this.m_cachedFinders = cachedFinders;
	}

	protected static void readProfile(CliParser cli, Analysis a, String filename) throws AnalysisCliException
	{
		FilePath output_path = a.getHomePath().chdir(getPathOfFile(filename));
		try
		{
			HardDisk hd = new HardDisk(output_path.toString()).open();
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
	 * Returns other command line arguments (not parsed).
	 *
	 * @return list of other arguments
	 */
	public List<String> getOthers()
	{
		return m_others;
	}

	/**
	 * Processes a batch of files provided by a FileProvider and creates the
	 * corresponding TokenFinderCallable tasks. This also incorporates any
	 * cached found tokens from cached finders.
	 *
	 * @param provider the FileProvider supplying files to analyze
	 * @param found    a set to which found tokens from caches will be added
	 * @return a set of TokenFinderCallable tasks to execute
	 * @throws IOException when an I/O error occurs
	 * @throws FileSystemException when filesystem operations fail
	 * @throws TokenFinderFactoryException when finder factory operations fail
	 */
	public Set<TokenFinderCallable> processBatch(FileProvider provider,
				Set<FoundToken> found) throws IOException, FileSystemException, TokenFinderFactoryException
	{
		List<FactoryCache> caches = checkCachedFinders();
		m_caches.addAll(caches);
		for (FactoryCache fc : caches)
		{
			found.addAll(fc.getFoundTokens());
		}
		int count = 0;
		Set<TokenFinderCallable> tasks = new HashSet<>();
		while (provider.hasNext() && (m_limit == -1 || count < m_limit))
		{
			count++;
			FileSource f_source = provider.next();
			if (m_ignoredFiles.contains(Paths.getFilename(f_source.getFilename())))
			{
				getStdout().println("Ignoring file " + f_source.getFilename());
				continue;
			}
			if (!m_visitorFinders.isEmpty())
			{
				VisitorAssertionFinderCallable r = new VisitorAssertionFinderCallable(m_projectName, f_source,
						m_visitorFinders, m_quiet, m_callback);
				tasks.add(r);
			}
			if (!m_sparqlFinders.isEmpty())
			{
				SparqlTokenFinderCallable r = new SparqlTokenFinderCallable(m_projectName, f_source,
						m_sparqlFinders, m_quiet, m_callback, m_follow);
				tasks.add(r);
			}
		}
		return tasks;
	}

	/**
	 * Checks which configured finders have cached results and returns the
	 * corresponding caches. This will remove factories that have valid caches
	 * from the active finder sets and add them to the cached set.
	 *
	 * @return a list of FactoryCache objects representing available caches
	 * @throws FileSystemException when filesystem operations fail
	 * @throws TokenFinderFactoryException when factory operations fail
	 */
	protected List<FactoryCache> checkCachedFinders() throws FileSystemException, TokenFinderFactoryException
	{
		List<FactoryCache> all_cached = new ArrayList<>();
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
				Iterator<VisitorAssertionFinderFactory> it = m_visitorFinders.iterator();
				while (it.hasNext())
				{
					VisitorAssertionFinderFactory f = it.next();
					if (f.isCached(hd, m_projectName))
					{
						// Verify cache integrity by trying to read it
						FactoryCache fc = f.readCache(hd, m_projectName, m_forceCache);
						if (fc == null || fc.getFoundTokens() == null)
						{
							// Cache is corrupt, ignore it
							continue;
						}
						else
						{
							m_cachedFinders.add(f);
							all_cached.add(fc);
							it.remove();
						}
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
						// Verify cache integrity by trying to read it
						FactoryCache fc = f.readCache(hd, m_projectName, m_forceCache);
						if (fc == null || fc.getFoundTokens() == null)
						{
							// Cache is corrupt, ignore it
							continue;
						}
						else
						{
							m_cachedFinders.add(f);
							all_cached.add(fc);
							it.remove();
						}
					}
				}
			}
			hd.close();
		}
		return all_cached;
	}

	/**
	 * Displays usage information using the provided CliParser and the analysis'
	 * configured stdout printer.
	 *
	 * @param cli the CliParser used to print help
	 */
	protected void showUsage(CliParser cli)
	{
		cli.printHelp("", m_stdout);
	}

	/**
	 * Reads queries (BeanShell or SPARQL) from a file or directory and
	 * populates the analysis with the corresponding finder factories. This
	 * method clears any previously configured AST and SPARQL finders.
	 *
	 * @param a   the Analysis instance to populate
	 * @param map the argument map containing the "query" option
	 * @throws AnalysisCliException if an error occurs while reading factories
	 */
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
				//a.getStdout().println("Reading queries from folder " + bsh_file);
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
					//a.getStdout().println("Reading SPARQL query from file " + bsh_file);
					hd.pushd(getPathOfFile(bsh_file).toString());
					SparqlTokenFinderFactory factory = SparqlTokenFinderFactory.readSparql(hd, bsh_file);
					hd.popd();
					a.getSparqlFinders().add(factory);
				}
				else if (bsh_file.endsWith(".bsh"))
				{
					//a.getStdout().println("Reading BeanShell script from file " + bsh_file);
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

	/**
	 * Reads lines from an InputStream, skipping blank lines and lines
	 * starting with '#'. The returned string concatenates the non-comment
	 * lines separated by spaces.
	 *
	 * @param is the InputStream to read
	 * @return a single String containing the non-comment lines separated by spaces
	 */
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
