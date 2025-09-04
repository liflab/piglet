package ca.uqac.lif.codefinder.util;

import java.util.Optional;
import java.util.concurrent.*;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;

public final class Types
{

	private Types()
	{
		/* no instances */ }

	/* ========= Result types ========= */

	public enum ResolveReason
	{
		RESOLVED, TIMEOUT, UNSOLVED
	}

	public static final class ResolveResult<T>
	{
		public final Optional<T> value;
		public final ResolveReason reason;

		private ResolveResult(Optional<T> value, ResolveReason reason)
		{
			this.value = value;
			this.reason = reason;
		}

		public static <T> ResolveResult<T> resolved(T v)
		{
			return new ResolveResult<>(Optional.of(v), ResolveReason.RESOLVED);
		}

		public static <T> ResolveResult<T> timeout()
		{
			return new ResolveResult<>(Optional.empty(), ResolveReason.TIMEOUT);
		}

		public static <T> ResolveResult<T> unsolved()
		{
			return new ResolveResult<>(Optional.empty(), ResolveReason.UNSOLVED);
		}
	}

	/* ========= No-timeout variant ========= */

	/**
	 * Resolve the type of an expression. Returns RESOLVED/UNSOLVED (no timeout).
	 */
	public static ResolveResult<ResolvedType> typeOf(Expression expr, TypeSolver ts)
	{
		try
		{
			ResolvedType t = JavaParserFacade.get(ts).getType(expr);
			return ResolveResult.resolved(t);
		}
		catch (UnsolvedSymbolException | UnsupportedOperationException e)
		{
			return ResolveResult.unsolved();
		}
		catch (RuntimeException e)
		{
			// Some versions wrap UnsolvedSymbolException; treat as UNSOLVED.
			if (e.getCause() instanceof UnsolvedSymbolException)
				return ResolveResult.unsolved();
			return ResolveResult.unsolved(); // or rethrow if you prefer to fail hard
		}
	}

	/* ========= Timeout variant ========= */

	// One single-thread executor per *worker thread* so we can cancel deep
	// resolution safely.
	private static final ThreadLocal<ExecutorService> RESOLVE_EXEC = ThreadLocal
			.withInitial(() -> Executors.newSingleThreadExecutor(r -> {
				Thread t = new Thread(r, "resolve-" + Thread.currentThread().getName());
				t.setDaemon(true);
				return t;
			}));

	/** Optional: call once at program end to cleanup the per-thread executors. */
	public static void shutdownResolveExecutor()
	{
		ExecutorService ex = RESOLVE_EXEC.get();
		ex.shutdownNow();
	}

	/**
	 * Resolve the type with a hard timeout. Returns RESOLVED / TIMEOUT / UNSOLVED.
	 */
	public static ResolveResult<ResolvedType> typeOfWithTimeout(Expression expr, TypeSolver ts,
			long timeoutMillis)
	{
		Future<ResolvedType> f = RESOLVE_EXEC.get()
				.submit(() -> JavaParserFacade.get(ts).getType(expr));
		try
		{
			ResolvedType t = f.get(timeoutMillis, TimeUnit.MILLISECONDS);
			return (t != null) ? ResolveResult.resolved(t) : ResolveResult.unsolved();
		}
		catch (TimeoutException te)
		{
			f.cancel(true); // interrupt deep resolution
			return ResolveResult.timeout();
		}
		catch (ExecutionException ee)
		{
			Throwable c = ee.getCause();
			if (c instanceof UnsolvedSymbolException || c instanceof UnsupportedOperationException)
			{
				return ResolveResult.unsolved();
			}
			return ResolveResult.unsolved();
		}
		catch (InterruptedException ie)
		{
			Thread.currentThread().interrupt();
			return ResolveResult.unsolved();
		}
	}
}
