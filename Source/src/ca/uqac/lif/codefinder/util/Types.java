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
package ca.uqac.lif.codefinder.util;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;

public final class Types
{
	private Types()
	{
		// Do nothing
		super();
	}

	/** Resolve the type of an expression or throw. */
	public static ResolvedType typeOf(Expression e, TypeSolver ts)
	{
		return JavaParserFacade.get(ts).getType(e);
	}

	public static Optional<ResolvedType> safeTypeOf(Expression e, TypeSolver ts)
	{
		try
		{
			return Optional.of(JavaParserFacade.get(ts).getType(e));
		}
		catch (UnsolvedSymbolException | UnsupportedOperationException ex)
		{
			return Optional.empty(); // don’t crash the run
		}
		catch (RuntimeException ex)
		{
			// some versions wrap UnsolvedSymbolException
			if (ex.getCause() instanceof UnsolvedSymbolException)
				return Optional.empty();
			throw ex;
		}
	}

	/** Resolve the declared type of a field/variable/parameter node. */
	public static ResolvedType declaredTypeOf(NodeWithType<?,?> nwt, TypeSolver ts)
	{
		Type t = (Type) nwt.getType();
		return JavaParserFacade.get(ts).convertToUsage(t);
	}

	/** Resolve the return type of a method call. */
	public static ResolvedType returnTypeOf(MethodCallExpr mce, TypeSolver ts)
	{
		ResolvedMethodDeclaration decl = JavaParserFacade.get(ts).solve(mce)
				.getCorrespondingDeclaration();
		return decl.getReturnType();
	}

	public static Optional<ResolvedType> returnTypeOrSkip(MethodCallExpr mce, TypeSolver ts)
	{
		try
		{
			ResolvedMethodDeclaration decl = JavaParserFacade.get(ts).solve(mce)
					.getCorrespondingDeclaration();
			return Optional.of(decl.getReturnType());
		}
		catch (RuntimeException ex)
		{
			if (mce.getScope().isPresent() && isLikelyTypeName(mce.getScope().get()))
			{
				return Optional.empty();
			}
			throw new IllegalStateException(diag("Could not resolve method call", mce, ex.getMessage()),
					ex);
		}
	}

	/** Quick syntactic name when available (no solver). */
	public static Optional<String> syntacticTypeName(NodeWithType<?, ?> nwt)
	{
		Type t = (Type) nwt.getType();
		if (t.isClassOrInterfaceType())
		{
			// Includes scope/qualifier when present
			return Optional.of(t.asClassOrInterfaceType().getNameWithScope());
		}
		return Optional.empty();
	}

	/**
	 * Coarse check: does a declared type (or its simple/qualified name) look like
	 * the target?
	 */
	public static boolean declaredTypeNameIs(NodeWithType<?, ?> nwt, String simpleOrQualified)
	{
		String want = simpleOrQualified;
		return syntacticTypeName(nwt).map(n -> n.equals(want) || n.endsWith("." + want)).orElse(false);
	}

	/**
	 * Coarse "is subtype named" without deep ancestry: 1) try declared/syntactic
	 * name match 2) optionally try solver with a bounded budget
	 */
	public static boolean isSubtypeNamedCoarse(Optional<ResolvedType> maybeResolvedType,
			NodeWithType<?,?> declaredIfAvailable, String targetFqn, TypeSolver ts, long timeoutMillis,
			int maxAncestorChecks)
	{

		// (1) Syntactic/declared fast path
		if (declaredIfAvailable != null && declaredTypeNameIs(declaredIfAvailable, targetFqn))
		{
			return true;
		}

		// (2) If a resolved type is already provided, use it. Otherwise, try to resolve
		// with a timeout.
		Optional<ResolvedType> rt = maybeResolvedType.isPresent() ? maybeResolvedType
				: convertDeclaredWithTimeout(declaredIfAvailable, ts, timeoutMillis);

		if (rt.isEmpty() || !rt.get().isReferenceType())
			return false;

		var ref = rt.get().asReferenceType();
		if (targetFqn.equals(ref.getQualifiedName()))
			return true;

		// Bounded ancestor scan (coarse)
		int hops = 0;
		for (var a : ref.getAllAncestors())
		{
			if (targetFqn.equals(a.getQualifiedName()))
				return true;
			if (++hops >= maxAncestorChecks)
				break; // cap depth
		}
		return false;
	}

	/* ---------- internals ---------- */

	@SuppressWarnings("unused")
	private static boolean shouldSkipTyping(Expression e)
	{
		if (e.getParentNode().isPresent() && e.getParentNode().get() instanceof MethodCallExpr)
		{
			MethodCallExpr m = (MethodCallExpr) e.getParentNode().get();
			if (m.getScope().isPresent() && m.getScope().get() == e && isLikelyTypeName(e))
				return true;
		}
		if (e.getParentNode().isPresent())
		{
			Node p = e.getParentNode().get();
			if (p instanceof com.github.javaparser.ast.expr.ObjectCreationExpr)
			{
				var oce = (com.github.javaparser.ast.expr.ObjectCreationExpr) p;
				if (oce.getScope().isPresent() && oce.getScope().get() == e)
					return true;
			}
			if (p instanceof com.github.javaparser.ast.expr.ClassExpr)
				return true;
			if (p instanceof com.github.javaparser.ast.expr.MethodReferenceExpr)
			{
				var mre = (com.github.javaparser.ast.expr.MethodReferenceExpr) p;
				if (mre.getScope() == e && isLikelyTypeName(e))
					return true;
			}
			if (p instanceof com.github.javaparser.ast.expr.AnnotationExpr)
				return true;
		}
		return false;
	}

	private static boolean isLikelyTypeName(Expression e)
	{
		if (e instanceof com.github.javaparser.ast.expr.NameExpr)
		{
			String s = ((com.github.javaparser.ast.expr.NameExpr) e).getNameAsString();
			return !s.isEmpty() && Character.isUpperCase(s.charAt(0));
		}
		if (e instanceof com.github.javaparser.ast.expr.FieldAccessExpr)
		{
			String s = ((com.github.javaparser.ast.expr.FieldAccessExpr) e).getNameAsString();
			return !s.isEmpty() && Character.isUpperCase(s.charAt(0));
		}
		return false;
	}

	private static String diag(String prefix, Node n, String detail)
	{
		String where = n.getRange().isPresent() ? n.getRange().get().toString() : "?";
		return prefix + " at " + where + " on " + n.getClass().getSimpleName() + ": " + detail;
	}

	// One single-thread executor per *worker thread* for cancellable resolution
	private static final ThreadLocal<ExecutorService> RESOLVE_EXEC = ThreadLocal
			.withInitial(() -> Executors.newSingleThreadExecutor(r -> {
				Thread t = new Thread(r, "resolve-" + Thread.currentThread().getName());
				t.setDaemon(true);
				return t;
			}));

	/** Optional: call once at program end to cleanup the per-thread executors. */
	public static void shutdownResolveExecutors()
	{
		ExecutorService ex = RESOLVE_EXEC.get();
		ex.shutdownNow();
	}

	/**
	 * Resolve MethodCall return type with a time budget; empty on timeout/failure.
	 */
	public static Optional<ResolvedType> returnTypeOfWithTimeout(MethodCallExpr mce, TypeSolver ts,
			long timeoutMillis)
	{
		return withTimeout(() -> {
			ResolvedMethodDeclaration decl = JavaParserFacade.get(ts).solve(mce)
					.getCorrespondingDeclaration();
			return decl.getReturnType();
		}, timeoutMillis);
	}

	/** Call once at the very end of your program if you want to clean up. */
	public static void shutdownResolveExecutor()
	{
		ExecutorService ex = RESOLVE_EXEC.get();
		ex.shutdownNow();
	}

	/**
	 * Smart type resolution: 1) If a declared type node is available (e.g.,
	 * var/param/field), try converting that (fast). 2) Otherwise (or if (1) fails),
	 * resolve the expression's type with a hard timeout. Returns empty on timeout
	 * or unsolved.
	 */
	public static Optional<ResolvedType> smartTypeOf(Expression expr,
			NodeWithType<?, ?> declaredIfAvailable, // pass null if not applicable
			TypeSolver ts, long timeoutMillis)
	{
		// 1) Coarse/cheap path: use declared AST type, if any.
		if (declaredIfAvailable != null)
		{
			Optional<ResolvedType> viaDeclared = convertDeclaredWithTimeout(declaredIfAvailable, ts,
					timeoutMillis / 2);
			if (viaDeclared.isPresent())
				return viaDeclared;
		}

		// 2) Fallback: full expression type with timeout.
		return typeOfWithTimeout(expr, ts, timeoutMillis);
	}

	/** Resolve an expression's type with a timeout (empty on timeout/unsolved). */
	public static Optional<ResolvedType> typeOfWithTimeout(Expression e, TypeSolver ts,
			long timeoutMillis)
	{
		return withTimeout(() -> JavaParserFacade.get(ts).getType(e), timeoutMillis);
	}

	/**
	 * Convert a declared AST type (field/var/param) with a timeout (empty on
	 * timeout/unsolved).
	 */
	public static Optional<ResolvedType> convertDeclaredWithTimeout(NodeWithType<?,?> nwt,
			TypeSolver ts, long timeoutMillis)
	{
		return withTimeout(() -> {
			Type t = (Type) nwt.getType();
			return JavaParserFacade.get(ts).convertToUsage(t);
		}, timeoutMillis);
	}

	// -------- private helpers --------

	private static <T> Optional<T> withTimeout(Callable<T> task, long timeoutMillis)
	{
		Future<T> f = RESOLVE_EXEC.get().submit(task);
		try
		{
			return Optional.ofNullable(f.get(timeoutMillis, TimeUnit.MILLISECONDS));
		}
		catch (TimeoutException te)
		{
			f.cancel(true); // stop deep resolution
			return Optional.empty();
		}
		catch (ExecutionException ee)
		{
			// Downgrade common "can't resolve" cases to empty; you can widen if needed.
			Throwable c = ee.getCause();
			if (c instanceof com.github.javaparser.resolution.UnsolvedSymbolException
					|| c instanceof UnsupportedOperationException)
			{
				return Optional.empty();
			}
			return Optional.empty();
		}
		catch (InterruptedException ie)
		{
			Thread.currentThread().interrupt();
			return Optional.empty();
		}
	}

}
