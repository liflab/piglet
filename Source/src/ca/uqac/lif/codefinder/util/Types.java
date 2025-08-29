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

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;

import java.util.Optional;

public final class Types
{
	private Types()
	{
	}

	/**
	 * Return a type, or Optional.empty() if this expression is a known, benign
	 * "skip" (e.g., type-name scope).
	 */
	public static Optional<ResolvedType> typeOfOrSkip(Expression e, TypeSolver ts)
	{
		if (shouldSkipTyping(e))
		{
			return Optional.empty();
		}
		try
		{
			return Optional.of(JavaParserFacade.get(ts).getType(e));
		}
		catch (RuntimeException ex)
		{
			throw fail("Could not resolve type", e, ex);
		}
	}

	/** Throwing variant that still skips benign cases (returns null if skipped). */
	public static ResolvedType strictTypeOfOrNullIfSkipped(Expression e, TypeSolver ts)
	{
		if (shouldSkipTyping(e))
		{
			return null; // explicit nullable for callers that prefer non-Optional
		}
		try
		{
			return JavaParserFacade.get(ts).getType(e);
		}
		catch (RuntimeException ex)
		{
			throw fail("Could not resolve type", e, ex);
		}
	}

	/**
	 * Resolve declared type (fields/locals/params) or throw; this one never skips.
	 */
	public static ResolvedType strictDeclaredTypeOf(Node n, TypeSolver ts)
	{
		try
		{
			if (n instanceof NodeWithType)
			{
				NodeWithType<?, ?> nwt = (NodeWithType<?, ?>) n;
				Type t = (Type) nwt.getType();
				return JavaParserFacade.get(ts).convertToUsage(t);
			}
			throw new IllegalStateException("Node has no declared type: " + n.getClass().getSimpleName());
		}
		catch (RuntimeException ex)
		{
			throw new IllegalStateException(diag("Could not resolve declared type", n, ex.getMessage()),
					ex);
		}
	}

	/**
	 * Method call return type; throws on failure, but skips if the call's *scope*
	 * is a benign type-name.
	 */
	public static Optional<ResolvedType> returnTypeOrSkip(MethodCallExpr mce, TypeSolver ts)
	{
		// If scope itself is a type name, we still want the call's return type; no need
		// to skip.
		try
		{
			ResolvedMethodDeclaration decl = JavaParserFacade.get(ts).solve(mce)
					.getCorrespondingDeclaration();
			return Optional.of(decl.getReturnType());
		}
		catch (RuntimeException ex)
		{
			// If the resolution failed because the scope is clearly not a value, skip;
			// otherwise throw
			if (mce.getScope().isPresent() && isLikelyTypeName(mce.getScope().get()))
			{
				return Optional.empty();
			}
			throw new IllegalStateException(diag("Could not resolve method call", mce, ex.getMessage()),
					ex);
		}
	}

	/* ===================== internals ===================== */

	private static boolean shouldSkipTyping(Expression e)
	{
		// 1) Scope of a method call: Set.of(...), Map.entry(...)
		if (e.getParentNode().isPresent() && e.getParentNode().get() instanceof MethodCallExpr)
		{
			MethodCallExpr m = (MethodCallExpr) e.getParentNode().get();
			if (m.getScope().isPresent() && m.getScope().get() == e && isLikelyTypeName(e))
			{
				return true;
			}
		}
		// 2) Scope of an object creation: pkg.Outer.Inner(...)
		if (e.getParentNode().isPresent() && e.getParentNode().get() instanceof ObjectCreationExpr)
		{
			ObjectCreationExpr oce = (ObjectCreationExpr) e.getParentNode().get();
			if (oce.getScope().isPresent() && oce.getScope().get() == e)
			{
				return true;
			}
		}
		// 3) Class literal: Foo.class — the NameExpr/FieldAccessExpr is a type
		// qualifier
		if (e.getParentNode().isPresent() && e.getParentNode().get() instanceof ClassExpr)
		{
			return true;
		}
		// 4) Method reference qualifier: List::of, MyType::factory
		if (e.getParentNode().isPresent() && e.getParentNode().get() instanceof MethodReferenceExpr)
		{
			MethodReferenceExpr mre = (MethodReferenceExpr) e.getParentNode().get();
			if (mre.getScope() == e && isLikelyTypeName(e))
			{
				return true;
			}
		}
		// 5) Annotation names (treated as NameExpr in some traversals)
		if (e.getParentNode().isPresent() && e.getParentNode().get() instanceof AnnotationExpr)
		{
			return true;
		}
		return false;
	}

	private static boolean isLikelyTypeName(Expression e)
	{
		// Heuristic: a bare NameExpr or FieldAccessExpr starting with uppercase token
		if (e instanceof NameExpr)
		{
			String s = ((NameExpr) e).getNameAsString();
			return !s.isEmpty() && Character.isUpperCase(s.charAt(0));
		}
		if (e instanceof FieldAccessExpr)
		{
			// e.g., java.util.Set (qualifier chain ends with capitalized identifier)
			String s = ((FieldAccessExpr) e).getNameAsString();
			return !s.isEmpty() && Character.isUpperCase(s.charAt(0));
		}
		return false;
	}

	private static IllegalStateException fail(String prefix, Node n, RuntimeException ex)
	{
		return new IllegalStateException(diag(prefix, n, ex.getMessage()), ex);
	}

	private static String diag(String prefix, Node n, String detail)
	{
		String where = n.getRange().isPresent() ? n.getRange().get().toString() : "?";
		return prefix + " at " + where + " on " + n.getClass().getSimpleName() + ": " + detail;
	}
}
