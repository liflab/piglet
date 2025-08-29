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
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;

import java.util.Optional;

/**
 * A utility class for working with types in JavaParser.
 */
public final class Types
{
	/** Private constructor to prevent instantiation */
	private Types()
	{
	}

	/**
	 * Resolve the type of an expression without throwing.
	 * 
	 * @param e
	 *          The expression to resolve
	 * @param ts
	 *          The type solver to use
	 * @return An optional resolved type; empty if resolution failed
	 */
	public static Optional<ResolvedType> safeTypeOf(Expression e, TypeSolver ts)
	{
		try
		{
			return Optional.of(JavaParserFacade.get(ts).getType(e));
		}
		catch (RuntimeException ex)
		{
			System.err.printf("[TypeError] %s %s at %s -> %s: %s%n", e.getClass().getSimpleName(),
					e.toString(), e.getRange().isPresent() ? e.getRange().get().toString() : "?",
					ex.getClass().getSimpleName(), ex.getMessage());
			return Optional.empty();
		}
	}

	/**
	 * Resolve the declared type of a node without throwing.
	 * 
	 * @param n
	 *          The node to resolve
	 * @param ts
	 *          The type solver to use
	 * @return An optional resolved type; empty if resolution failed or if the node
	 *         has no declared type
	 */
	public static Optional<ResolvedType> safeDeclaredTypeOf(Node n, TypeSolver ts)
	{
		try
		{
			if (n instanceof NodeWithType)
			{
				NodeWithType<?, ?> nwt = (NodeWithType<?, ?>) n;
				Type t = (Type) nwt.getType();
				return Optional.of(JavaParserFacade.get(ts).convertToUsage(t));
			}
			return Optional.empty();
		}
		catch (RuntimeException ex)
		{
			System.err.printf("[DeclTypeError] %s at %s -> %s: %s%n", n.getClass().getSimpleName(),
					n.getRange().isPresent() ? n.getRange().get().toString() : "?",
					ex.getClass().getSimpleName(), ex.getMessage());
			return Optional.empty();
		}
	}

	/**
	 * Resolve the return type of a method call expression without throwing.
	 * 
	 * @param mce
	 *          The method call expression to resolve
	 * @param ts
	 *          The type solver to use
	 * @return An optional resolved type; empty if resolution failed
	 */
	public static Optional<ResolvedType> safeReturnTypeOf(MethodCallExpr mce, TypeSolver ts)
	{
		try
		{
			ResolvedMethodDeclaration decl = JavaParserFacade.get(ts).solve(mce)
					.getCorrespondingDeclaration();
			return Optional.of(decl.getReturnType());
		}
		catch (RuntimeException ex)
		{
			System.err.printf("[MethodSolveError] %s at %s -> %s: %s%n", mce.toString(),
					mce.getRange().isPresent() ? mce.getRange().get().toString() : "?",
					ex.getClass().getSimpleName(), ex.getMessage());
			return Optional.empty();
		}
	}

}
