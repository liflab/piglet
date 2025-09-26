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
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jena.query.QueryParseException;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;

import bsh.EvalError;
import ca.uqac.lif.azrael.PrintException;
import ca.uqac.lif.azrael.json.JsonPrinter;
import ca.uqac.lif.fs.FilePath;
import ca.uqac.lif.fs.FileSystem;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.FileUtils;
import ca.uqac.lif.fs.HardDisk;
import ca.uqac.lif.piglet.Analysis.AnalysisCliException;
import ca.uqac.lif.piglet.find.FoundToken;
import ca.uqac.lif.piglet.find.TokenFinderCallable;
import ca.uqac.lif.piglet.find.TokenFinderContext;
import ca.uqac.lif.piglet.find.TokenFinderFactory;
import ca.uqac.lif.piglet.find.TokenFinderCallable.CallableFuture;
import ca.uqac.lif.piglet.find.TokenFinderFactory.TokenFinderFactoryException;
import ca.uqac.lif.piglet.provider.FileSystemProvider;
import ca.uqac.lif.piglet.provider.UnionProvider;
import ca.uqac.lif.piglet.report.CliReporter;
import ca.uqac.lif.piglet.report.HtmlReporter;
import ca.uqac.lif.piglet.report.Report;
import ca.uqac.lif.piglet.report.Report.MapReport;
import ca.uqac.lif.piglet.report.Report.ObjectReport;
import ca.uqac.lif.piglet.report.Reporter.ReporterException;
import ca.uqac.lif.piglet.util.Solvers;
import ca.uqac.lif.piglet.util.StatusCallback;
import ca.uqac.lif.util.AnsiPrinter;
import ca.uqac.lif.util.AnsiPrinter.Color;
import ca.uqac.lif.util.CliParser;

/**
 * Main class of the CodeFinder application. Parses command line arguments, sets
 * up the environment, and launches the analysis.
 */
public class Main
{
	//Per-project snapshot of found tokens
	private static final Map<String, Set<FoundToken>> PER_PROJECT =
			Collections.synchronizedMap(new LinkedHashMap<>());

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
	 * Return code indicating a timeout
	 */
	public static final int RET_TIMEOUT = 6;

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
	 * The set of found tokens (shared by all threads)
	 */
	static final Set<FoundToken> found = Collections.synchronizedSortedSet(new TreeSet<>());

	// Runs-at-most-once guard for final reporting
	private static final AtomicBoolean FINALIZED = new AtomicBoolean(false);

	// Hold current run state (visible to the shutdown hook)
	private static volatile ExecutorService CURRENT_EXECUTOR;
	private static volatile Analysis CURRENT_ANALYSIS;
	private static volatile List<Future<CallableFuture>> CURRENT_FUTURES;

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

		/* Print greeting */
		printGreeting();

		/* Setup command line options */
		CliParser cli = Analysis.setupCli();
		final Set<Analysis> analyses = new TreeSet<>();
		try
		{
			Analysis.read(analyses, cli, cli.parse(args), s_stdout, s_stderr);
		}
		catch (AnalysisCliException e)
		{
			return handleException(e);
		}
		/* Adding a shutdown hook to display/save partial results if interrupted */
		Thread printingHook = new Thread(() -> {
			try
			{
				s_stderr.println();
				s_stderr.clearLine();
				s_stderr.bg(Color.RED).fg(Color.WHITE);
				s_stderr.println("Interrupted: harvesting completed tasks and generating partial results...");
				s_stderr.resetColors();

				// 1) Stop executor promptly (best effort)
				ExecutorService ex = CURRENT_EXECUTOR;
				if (ex != null)
				{
					ex.shutdownNow(); // don't block
				}

				// 2) Harvest whatever is already done (no blocking)
				List<Future<CallableFuture>> fs = CURRENT_FUTURES;
				Analysis a = CURRENT_ANALYSIS;
				if (fs != null && a != null)
				{
					for (Future<CallableFuture> f : fs)
					{
						if (f.isDone() && !f.isCancelled())
						{
							try
							{
								CallableFuture cf = f.get(); // safe: already done
								found.addAll(cf.getFoundTokens());
							}
							catch (Throwable t)
							{
								// ignore in shutdown
							}
						}
					}
				}
				Analysis an = CURRENT_ANALYSIS;
				if (an != null) {
				  synchronized (found) {
				    PER_PROJECT.put(an.getProjectName(), new TreeSet<>(found));
				  }
				}

				// 3) Same finalization path as normal completion
				MapReport global = new MapReport();
				finalizeAndReport(analyses, global, /* summary */ true);
			}
			catch (Throwable ignored)
			{
				// best-effort only
			}
			finally
			{
				try
				{
					s_stdout.flush();
				}
				catch (Throwable ignored)
				{
				}
				try
				{
					s_stderr.flush();
				}
				catch (Throwable ignored)
				{
				}
			}
		}, "piglet-shutdown");
		Runtime.getRuntime().addShutdownHook(printingHook);
		for (Analysis a : analyses) {
		  // Start this analysis with a clean bag
		  synchronized (found) { found.clear(); }

		  int ret = runAnalysis(a);
		  if (ret != RET_OK) return ret;

		  // Snapshot results for THIS project
		  synchronized (found) {
		    PER_PROJECT.put(a.getProjectName(), new TreeSet<>(found));
		  }
		}

		// Remove the shutdown hook, we are done
		Runtime.getRuntime().removeShutdownHook(printingHook);
		MapReport global = new MapReport();
		// Normal completion: do the same finalization as the hook, but feel free to
		// show full output
		finalizeAndReport(analyses, global, true);
		return RET_OK;
	}

	protected static int runAnalysis(Analysis analysis) throws FileSystemException, IOException,
	InterruptedException, ExecutionException, PrintException
	{
		/* Setup the file provider */
		FileSystemProvider[] providers = new FileSystemProvider[analysis.getSourcePaths().size()];
		int i = 0;
		for (String path : analysis.getSourcePaths())
		{
			FilePath fold_path = analysis.getHomePath().chdir(new FilePath(path));
			try
			{
				providers[i] = new FileSystemProvider(new HardDisk(fold_path.toString()));
			}
			catch (FileSystemException e)
			{
				return handleException(e);
			}
			i++;
		}
		UnionProvider fsp = new UnionProvider(providers);
		int total = fsp.filesProvided();
		Report.MapReport categorized = new Report.MapReport();
		categorized.put(analysis.getProjectName(), new MapReport());
		final List<String> source_paths = analysis.getSourcePaths();
		final String[] root = analysis.getRoots();
		final Set<String> jar_paths = analysis.getJarPaths();
		final long resolution_timeout = analysis.getResolutionTimeout();
		CTX = ThreadLocal.withInitial(() -> {
			try
			{
				CombinedTypeSolver ts = Solvers.buildSolver(source_paths, root, jar_paths);

				// Wire parser to THIS thread’s solver
				ParserConfiguration threadPc = new ParserConfiguration()
						.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17)
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
		if (analysis.m_globalTimeout > 0)
		{
			s_stdout.bg(Color.YELLOW).fg(Color.BLACK);
			s_stdout.println("Global timeout: " + analysis.m_globalTimeout + " seconds");
			s_stdout.resetColors();
		}
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
			Set<TokenFinderCallable> tasks = analysis.processBatch(fsp, found);
			// Submit tasks one by one so we can expose futures *immediately*
			List<Future<CallableFuture>> futures = new java.util.ArrayList<>(tasks.size());
			for (TokenFinderCallable task : tasks)
			{
				futures.add(executor.submit(task));
			}
			// Expose futures so the hook can harvest completed ones
			CURRENT_EXECUTOR = executor;
			CURRENT_ANALYSIS = analysis;
			CURRENT_FUTURES = futures;
			if (analysis.m_globalTimeout > 0)
			{
				// Cancels any still-running futures after the global window
				Executors.newSingleThreadScheduledExecutor().schedule(() -> {
					for (Future<?> f : futures)
					{
						if (!f.isDone())
							f.cancel(true);
					}
					executor.shutdownNow();
				}, analysis.m_globalTimeout, TimeUnit.SECONDS);
			}
			waitForEnd(status, analysis, futures, found);
			executor.shutdown();
		}
		catch (IOException e)
		{
			return handleException(e);
		}
		catch (FileSystemException e)
		{
			return handleException(e);
		}
		catch (TokenFinderFactoryException e)
		{
			return handleException(e);
		}
		try
		{
			if (!executor.awaitTermination(analysis.m_fileTimeout, TimeUnit.SECONDS))
			{
				executor.shutdownNow();
				if (!executor.awaitTermination(analysis.m_fileTimeout, TimeUnit.SECONDS))
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
		// end_callback.setTotal(found.size());
		long duration = end_time - start_time;
		// After finishing this analysis, clear the pointers (normal path)
		CURRENT_FUTURES = null;
		CURRENT_ANALYSIS = null;
		CURRENT_EXECUTOR = null;
		status.cleanup();
		s_stdout
		.println((analysis.getLimit() >= 0 ? analysis.getLimit() : total) + " file(s) analyzed");
		s_stdout.println(found.size() + " token" + (found.size() > 1 ? "s" : "") + " found");
		s_stdout.clearLine();
		s_stdout.println("Analysis time: " + AnsiPrinter.formatDuration(duration));
		s_stdout.println();
		return RET_OK;
	}

	/**
	 * Takes care of the final steps of the analysis: categorization of results,
	 * report generation, and serialization of results. This step is done even if
	 * the analysis was interrupted. In that case, only the results accumulated so
	 * far are processed.
	 * 
	 * @param categorized
	 * @param global
	 * @param analysis
	 * @param found
	 * @return A return code, typically {@link #RET_OK}
	 * @throws IOException
	 * @throws PrintException
	 */
	protected static int finish(MapReport categorized, MapReport global, Analysis analysis,
			Set<FoundToken> found) throws IOException, PrintException
	{
		/* Categorize results and produce report */
		categorize(analysis.getProjectName(), categorized, found);
		// TODO: eventually merge with global results
		categorize(analysis.getProjectName(), global, found);
		FilePath output_path = analysis.getHomePath().chdir(getPathOfFile(analysis.getOutputFile()));
		String s_source_path = analysis.getSourcePaths().get(0);
		Path p = Paths.get(s_source_path).toAbsolutePath().normalize();
		FilePath reverse_path = output_path.chdir(new FilePath(p.toString()));
		HardDisk hd;
		try
		{
			hd = new HardDisk(output_path.toString()).open();
			HtmlReporter reporter = new HtmlReporter(
					new PrintStream(hd.writeTo(getFilename(analysis.getOutputFile())), true, "UTF-8"));
			reporter.report(reverse_path, categorized);
			hd.close();
			hd = new HardDisk(analysis.getHomePath().toString()).open();
			if (!hd.isDirectory(analysis.getCacheFolder()))
			{
				hd.mkdir(analysis.getCacheFolder());
			}
			hd.close();
			hd = new HardDisk(analysis.getCacheFolder()).open();
			serializeResults(hd, analysis, categorized);
			hd.close();
		}
		catch (ReporterException e)
		{
			return handleException(e);
		}
		catch (FileSystemException e)
		{
			return handleException(e);
		}
		return RET_OK;
	}

	/**
	 * Categorizes the found tokens by assertion name (i.e., the name of the
	 * assertion that produced them).
	 * 
	 * @param map
	 *          The map to populate
	 * @param found
	 *          The set of found tokens
	 */
	protected static void categorize(String project, MapReport r, Set<FoundToken> found)
	{
		for (FoundToken t : found)
		{
			String key = project + Report.PATH_SEPARATOR + t.getAssertionName();
			r.append(key, t);
		}
	}

	@SuppressWarnings("unchecked")
	protected static void serializeResults(FileSystem fs, Analysis a, MapReport r)
			throws PrintException, FileSystemException
	{
		for (String project : r.keySet())
		{
			if (!fs.isDirectory(project))
			{
				fs.mkdir(project);
			}
			// fs.pushd(project);
			MapReport entries = (MapReport) r.get(project);
			for (TokenFinderFactory tf : a.getAstFinders())
			{
				if (a.m_cache && a.isCached(tf.getName()))
				{
					// Already cached, skip
					continue;
				}
				ObjectReport or = (ObjectReport) entries.get(tf.getName());
				List<FoundToken> list = null;
				if (or != null)
				{
					list = (List<FoundToken>) or.getObject();
				}
				else
				{
					list = new java.util.ArrayList<>();
				}
				JsonPrinter xp = new JsonPrinter();
				List<Object> to_serialize = new java.util.ArrayList<>();
				to_serialize.add(tf.getId());
				to_serialize.add(list);
				String s = xp.print(to_serialize).toString();
				FileUtils.writeStringTo(fs, s, tf.getCacheFileName(project));
			}
			for (TokenFinderFactory tf : a.getSparqlFinders())
			{
				if (a.m_cache && a.isCached(tf.getName()))
				{
					// Already cached, skip
					continue;
				}
				ObjectReport or = (ObjectReport) entries.get(tf.getName());
				List<FoundToken> list = null;
				if (or != null)
				{
					list = (List<FoundToken>) or.getObject();
				}
				else
				{
					list = new java.util.ArrayList<>();
				}
				JsonPrinter xp = new JsonPrinter();
				List<Object> to_serialize = new java.util.ArrayList<>();
				to_serialize.add(tf.getId());
				to_serialize.add(list);
				String s = xp.print(to_serialize).toString();
				FileUtils.writeStringTo(fs, s, tf.getCacheFileName(project));
			}
			// fs.popd();
		}
	}

	/**
	 * Handles an exception by printing an appropriate message to standard error and
	 * returning an appropriate return code.
	 * 
	 * @param e
	 *          The exception to handle
	 * @return An appropriate return code
	 */
	protected static int handleException(TokenFinderFactoryException e)
	{
		Throwable t = e.getCause();
		return handleCause(t);
	}

	/**
	 * Handles an I/O exception by printing an appropriate message to standard error
	 * and returning an appropriate return code.
	 * 
	 * @param e
	 *          The exception to handle
	 * @return An appropriate return code
	 */
	protected static int handleException(IOException e)
	{
		s_stderr.println("I/O error: " + e.getMessage());
		return RET_IO;
	}

	/**
	 * Handles a reporting exception by printing an appropriate message to standard
	 * error and returning an appropriate return code.
	 * 
	 * @param e
	 *          The exception to handle
	 * @return An appropriate return code
	 */
	protected static int handleException(ReporterException e)
	{
		Throwable t = e.getCause();
		return handleCause(t);
	}

	/**
	 * Handles a cause exception by printing an appropriate message to standard
	 * error and returning an appropriate return code.
	 * 
	 * @param t
	 *          The exception to handle
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
	 * Handles an analysis exception by printing an appropriate message to standard
	 * error and returning an appropriate return code.
	 * 
	 * @param e
	 *          The exception to handle
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
	 * Handles a file system exception by printing an appropriate message to
	 * standard error and returning an appropriate return code.
	 * 
	 * @param e
	 *          The exception to handle
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
	public static boolean waitForEnd(StatusCallback callback, Analysis a,
			List<Future<CallableFuture>> futures, Set<FoundToken> found)
	{
		Iterator<Future<CallableFuture>> it = futures.iterator();
		while (it.hasNext())
		{
			Future<CallableFuture> f = it.next();
			try
			{
				CallableFuture cf = f.get(a.m_fileTimeout, TimeUnit.SECONDS);
				found.addAll(cf.getFoundTokens());
			}
			catch (TimeoutException te)
			{
				f.cancel(true);
				it.remove();
				callback.resolutionTimeout();
			}
			catch (InterruptedException ie)
			{
				Thread.currentThread().interrupt();
				// If interrupted, you can choose to cancel outstanding tasks:
				for (Future<?> other : futures)
				{
					other.cancel(true);
				}
				break;
			}
			catch (CancellationException ce)
			{
				// Task was cancelled, probably due to global timeout
				// This should trigger the shutdown hook, which will do the final reporting
				System.exit(RET_TIMEOUT);
			}
			catch (QueryParseException qpe)
			{
				s_stderr.println("Cannot parse query: " + qpe.getMessage());
			}
			catch (ExecutionException ee)
			{
				// The task threw; unwrap and either log or fail fast
				s_stderr.println("Error in task: " + a.getFileForFuture(f));
				s_stderr.println("Cause: " + ee.getCause().getMessage());
				ee.printStackTrace(s_stderr);
			}
		}
		return true;
	}

	private static void finalizeAndReport(Set<Analysis> analyses, Report.MapReport global,
			boolean summary)
	{
		if (!FINALIZED.compareAndSet(false, true))
			return;

		// Stop status line and make room
		try
		{
			s_stdout.clearLine();
		}
		catch (Throwable ignored)
		{
		}

		// Build reports using whatever is in 'found' right now
		s_stdout.println(found.size() + " token" + (found.size() != 1 ? "s" : "") + " found total");
		synchronized (found)
		{
			for (Analysis analysis : analyses)
			{
		    Set<FoundToken> per = PER_PROJECT.getOrDefault(analysis.getProjectName(), Collections.emptySet());
				try
				{
					finish(new Report.MapReport(), global, analysis, per);
				}
				catch (IOException | PrintException e)
				{
					handleException(e instanceof IOException ? (IOException) e : new IOException(e));
				}
			}
		}

		try
		{
			// Print the CLI report to stderr (more reliable in shutdown)
			CliReporter cli = new CliReporter(s_stdout, summary);
			cli.report(null, global);
		}
		catch (ReporterException e)
		{
			handleException(e);
		}

		// Strong flush at the very end
		try
		{
			s_stdout.flush();
		}
		catch (Throwable ignored)
		{
		}
		try
		{
			s_stderr.flush();
		}
		catch (Throwable ignored)
		{
		}
	}

	/**
	 * Prints a greeting to standard output, if the terminal supports it.
	 */
	protected static void printGreeting()
	{
		s_stdout.fg(Color.LIGHT_PURPLE);
		s_stdout.italics();
		s_stdout.print("Piglet");
		s_stdout.fg(Color.PURPLE);
		s_stdout.print(" v1.0");
		s_stdout.unitalics();
		s_stdout.resetColors();
		s_stdout.println(" - Analysis of Java source code");
		s_stdout.println(
				"\u00A9 2025 Laboratoire d'informatique formelle, Universit\u00E9 du Qu\u00E9bec \u00E0 Chicoutimi");

		/*
		 * if (Terminal.likelySupportsSixel()) { try { String sixel_data = new
		 * String(FileUtils.toBytes(Main.class.getResourceAsStream("Piglet_crop.sixel"))
		 * ); s_stdout.printBytes(sixel_data); } catch (FileSystemException e) { //
		 * Don't care, this is cosmetic } }
		 */
	}

}
