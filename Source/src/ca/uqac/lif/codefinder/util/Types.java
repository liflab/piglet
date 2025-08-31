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

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
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

	/** Resolve the declared type of a field/variable/parameter node. */
	public static ResolvedType declaredTypeOf(NodeWithType<?, ?> nwt, TypeSolver ts)
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
}
