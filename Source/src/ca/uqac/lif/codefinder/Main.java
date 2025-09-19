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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;

import bsh.EvalError;
import ca.uqac.lif.azrael.PrintException;
import ca.uqac.lif.azrael.json.JsonPrinter;
import ca.uqac.lif.codefinder.Analysis.AnalysisCliException;
import ca.uqac.lif.codefinder.find.FoundToken;
import ca.uqac.lif.codefinder.find.TokenFinderContext;
import ca.uqac.lif.codefinder.find.TokenFinderFactory.TokenFinderFactoryException;
import ca.uqac.lif.codefinder.provider.FileSystemProvider;
import ca.uqac.lif.codefinder.provider.UnionProvider;
import ca.uqac.lif.codefinder.report.CliReporter;
import ca.uqac.lif.codefinder.report.HtmlReporter;
import ca.uqac.lif.codefinder.report.Report;
import ca.uqac.lif.codefinder.report.Report.MapReport;
import ca.uqac.lif.codefinder.report.Report.ObjectReport;
import ca.uqac.lif.codefinder.report.Reporter.ReporterException;
import ca.uqac.lif.codefinder.util.AnsiPrinter;
import ca.uqac.lif.codefinder.util.Solvers;
import ca.uqac.lif.codefinder.util.StatusCallback;
import ca.uqac.lif.fs.FilePath;
import ca.uqac.lif.fs.FileSystem;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.FileUtils;
import ca.uqac.lif.fs.HardDisk;
import ca.uqac.lif.util.CliParser;

/**
 * Main class of the CodeFinder application. Parses command line arguments, sets
 * up the environment, and launches the analysis.
 */
public class Main
{
	/**
	 * Return code indicating "no return"
	 */
	public static final int RET_NOTHING = -1;

	/**
	 * Return code indicating successful execution
	 */
	public static final int RET_OK = 0;

	/**
	 * Return code indicating a file system error
	 */
	public static final int RET_FS = 1;

	/**
	 * Return code indicating an I/O error
	 */
	public static final int RET_IO = 2;

	/**
	 * Return code indicating a BeanShell error
	 */
	public static final int RET_BSH = 3;

	/**
	 * Return code indicating an error in the command line
	 */
	public static final int RET_CLI = 4;

	/**
	 * Return code indicating an unclassified error
	 */
	public static final int RET_OTHER = 5;

	/**
	 * Standard output
	 */
	protected static final AnsiPrinter s_stdout = new AnsiPrinter(System.out);

	/**
	 * Standard error
	 */
	protected static final AnsiPrinter s_stderr = new AnsiPrinter(System.err);

	/** 
	 * Thread-local context (parser, type solver, etc.)
	 */
	public static ThreadLocal<TokenFinderContext> CTX;


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
		int out = doMain(args);
		System.exit(out);
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
		CliParser cli = Analysis.setupCli();
		Analysis analysis = null;
		try
		{
			analysis = Analysis.read(cli,  cli.parse(args), s_stdout, s_stderr);	
		}
		catch (AnalysisCliException e)
		{
			return handleException(e);
		}

		/* Setup the file provider */
		List<String> folders = analysis.getOthers(); // The files to read from
		FileSystemProvider[] providers = new FileSystemProvider[folders.size()];
		for (int i = 0; i < folders.size(); i++)
		{
			FilePath fold_path = analysis.getHomePath().chdir(new FilePath(folders.get(i)));
			try
			{
				providers[i] = new FileSystemProvider(new HardDisk(fold_path.toString()));
			}
			catch (FileSystemException e)
			{
				return handleException(e);
			}
		}
		UnionProvider fsp = new UnionProvider(providers);
		int total = fsp.filesProvided();
		Report.MapReport categorized = new Report.MapReport();
		Set<FoundToken> found = new HashSet<>();
		EndRunnable end_callback = new EndRunnable(categorized, found.size(), analysis.getSummary());
		Runtime.getRuntime().addShutdownHook(new Thread(end_callback));
		final Set<String> source_paths = analysis.getSourcePaths();
		final String root = analysis.getRoot();
		final Set<String> jar_paths = analysis.getJarPaths();
		final long resolution_timeout = analysis.getResolutionTimeout();
		CTX = ThreadLocal.withInitial(() -> {
			try
			{
				CombinedTypeSolver ts = Solvers.buildSolver(source_paths, root, jar_paths);

				// Wire parser to THIS thread’s solver
				ParserConfiguration threadPc = new ParserConfiguration()
						.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_11)
						.setSymbolResolver(new com.github.javaparser.symbolsolver.JavaSymbolSolver(ts));

				return new TokenFinderContext(ts, new JavaParser(threadPc),
						com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade.get(ts),
						resolution_timeout);
			}
			catch (Exception e)
			{
				throw new RuntimeException("Failed to init per-thread context", e);
			}
		});

		// Read file(s)
		StatusCallback status = new StatusCallback(s_stdout,
				(analysis.getLimit() >= 0 ? Math.min(total, analysis.getLimit()) : total));
		analysis.setCallback(status);
		Thread status_thread = new Thread(status);
		AtomicInteger THREAD_ID = new AtomicInteger(1);
		ThreadFactory tf = r -> {
			Thread t = new Thread(r);
			t.setName("anl-" + THREAD_ID.incrementAndGet());
			t.setDaemon(false);
			return t;
		};

		ExecutorService executor = Executors.newFixedThreadPool(analysis.getThreads(), tf);
		long start_time = System.currentTimeMillis();
		long end_time = -1;
		s_stdout.hideCursor();
		status_thread.start();
		try
		{
			List<Future<Set<FoundToken>>> futures = analysis.processBatch(executor, fsp, found);
			waitForEnd(futures);
			executor.shutdown();
			for (Future<Set<FoundToken>> f : futures)
			{
				found.addAll(f.get());
			}
		}
		catch (IOException e)
		{
			handleException(e);
		}
		catch (FileSystemException e)
		{
			handleException(e);
		}
		catch (TokenFinderFactoryException e)
		{
			handleException(e);
		}
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
		end_callback.setTotal(found.size());
		long duration = end_time - start_time;
		status.cleanup();
		s_stdout.println((analysis.getLimit() >= 0 ? analysis.getLimit() : total) + " file(s) analyzed");
		s_stdout.println(found.size() + " assertion(s) found");
		s_stdout.clearLine();
		s_stdout.println("Analysis time: " + AnsiPrinter.formatDuration(duration));
		s_stdout.println();

		/* Categorize results and produce report */
		categorize(analysis.getProjectName(), categorized, found);
		FilePath output_path = analysis.getHomePath().chdir(getPathOfFile(analysis.getOutputFile()));
		FilePath reverse_path = output_path.chdir(new FilePath(folders.get(0)));
		HardDisk hd;
		try
		{
			hd = new HardDisk(output_path.toString()).open();
			HtmlReporter reporter = new HtmlReporter(
					new PrintStream(hd.writeTo(getFilename(analysis.getOutputFile())), true, "UTF-8"));
			reporter.report(reverse_path, categorized);
			hd.close();
			hd = new HardDisk(analysis.getCacheFolder()).open();
			serializeResults(hd, categorized);
			hd.close();
		}
		catch (ReporterException e)
		{
			return handleException(e);
		}
		return RET_OK;
	}
	
	/**
	 * Categorizes the found tokens by assertion name (i.e., the name of the
	 * assertion that produced them).
	 * @param map The map to populate
	 * @param found The set of found tokens
	 */
	protected static void categorize(String project, MapReport r, Set<FoundToken> found)
	{
		for (FoundToken t : found)
		{
			String key = project + Report.PATH_SEPARATOR + t.getAssertionName();
			r.append(key, t);
		}
	}

	protected static void serializeResults(FileSystem fs, MapReport r)
			throws PrintException, FileSystemException
	{
		for (String project : r.keySet())
		{
			if (!fs.isDirectory(project))
			{
				fs.mkdir(project);
			}
			fs.pushd(project);
			MapReport entries = (MapReport) r.get(project);
			for (String name : entries.keySet())
			{
				ObjectReport or = (ObjectReport) entries.get(name);
				@SuppressWarnings("unchecked")
				List<FoundToken> list = (List<FoundToken>) or.getObject();
				JsonPrinter xp = new JsonPrinter();
				String s = xp.print(list).toString();
				FileUtils.writeStringTo(fs, s, name + ".json");
			}
			fs.popd();
		}
	}

	/**
	 * Handles an exception by printing an appropriate message to standard error and
	 * returning an appropriate return code.
	 * @param e The exception to handle
	 * @return An appropriate return code
	 */
	protected static int handleException(TokenFinderFactoryException e)
	{
		Throwable t = e.getCause();
		return handleCause(t);
	}

	/**
	 * Handles an I/O exception by printing an appropriate message to standard error and
	 * returning an appropriate return code.
	 * @param e The exception to handle
	 * @return An appropriate return code
	 */
	protected static int handleException(IOException e)
	{
		s_stderr.println("I/O error: " + e.getMessage());
		return RET_IO;
	}

	/**
	 * Handles a reporting exception by printing an appropriate message to standard error and
	 * returning an appropriate return code.
	 * @param e The exception to handle
	 * @return An appropriate return code
	 */
	protected static int handleException(ReporterException e)
	{
		Throwable t = e.getCause();
		return handleCause(t);
	}

	/**
	 * Handles a cause exception by printing an appropriate message to standard error and
	 * returning an appropriate return code.
	 * @param t The exception to handle
	 * @return An appropriate return code
	 */
	protected static int handleCause(Throwable t)
	{
		if (t instanceof FileSystemException)
		{
			s_stderr.println("File system error: " + t.getMessage());
			return RET_FS;
		}
		else if (t instanceof IOException)
		{
			s_stderr.println("I/O error: " + t.getMessage());
			return RET_IO;
		}
		else if (t instanceof EvalError)
		{
			s_stderr.println("BeanShell error: " + t.getMessage());
			return RET_BSH;
		}
		else
		{
			s_stderr.println("Error: " + t.getMessage());
			return RET_OTHER;
		}
	}

	/**
	 * Handles an analysis exception by printing an appropriate message to standard error and
	 * returning an appropriate return code.
	 * @param e The exception to handle
	 * @return An appropriate return code
	 */
	protected static int handleException(AnalysisCliException e)
	{
		Throwable t = e.getCause();
		if (t instanceof FileSystemException)
		{
			s_stderr.println("File system error: " + t.getMessage());
			return RET_FS;
		}
		else if (t instanceof IOException)
		{
			s_stderr.println("I/O error: " + t.getMessage());
			return RET_IO;
		}
		else if (t instanceof EvalError)
		{
			s_stderr.println("BeanShell error: " + t.getMessage());
			return RET_BSH;
		}
		else
		{
			s_stderr.println("Error: " + e.getMessage());
			return RET_OTHER;
		}
	}

	/**
	 * Handles a file system exception by printing an appropriate message to standard error and
	 * returning an appropriate return code.
	 * @param e The exception to handle
	 * @return An appropriate return code
	 */
	protected static int handleException(FileSystemException e)
	{
		s_stderr.println("File system error: " + e.getMessage());
		return RET_FS;
	}

	/**
	 * Waits for the completion of a list of futures, handling exceptions and
	 * interruptions properly.
	 * 
	 * @param futures
	 *          The list of futures to wait for
	 */
	public static void waitForEnd(List<Future<Set<FoundToken>>> futures)
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

	/**
	 * Runnable that displays the results at the end of the analysis.
	 */
	protected static class EndRunnable implements Runnable
	{
		private final Report m_found;

		private int m_total;

		private final boolean m_summary;

		public EndRunnable(Report found, int total, boolean summary)
		{
			super();
			m_found = found;
			m_summary = summary;
			m_total = total;
		}

		public void setTotal(int total)
		{
			m_total = total;
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
				cli_reporter.report(null, m_found);
			}
			catch (ReporterException e)
			{
				// Ignore for the moment
			}
		}
	}
}
