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
package ca.uqac.lif.piglet.util;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.ResolvedTypeVariable;
import com.github.javaparser.resolution.types.ResolvedWildcard;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility methods to check types.
 */
public final class TypeChecks
{
  private static final Map<String, ResolvedType> CACHE = new ConcurrentHashMap<>();
  
	private TypeChecks()
	{
		super();
	}

	/**
	 * True if `t` is (or extends/implements) the target FQN (e.g.,
	 * "java.util.Map").
	 */
	public static boolean isSubtypeOf(ResolvedType t, String targetFqn)
	{
		Objects.requireNonNull(t, "type");
		Objects.requireNonNull(targetFqn, "targetFqn");

		// 1) Concrete class/interface types (with generics)
		if (t.isReferenceType())
		{
			ResolvedReferenceType ref = t.asReferenceType();
			if (targetFqn.equals(ref.getQualifiedName()))
				return true;
			// check all ancestors (superclass + interfaces)
			for (ResolvedReferenceType a : ref.getAllAncestors())
			{
				if (targetFqn.equals(a.getQualifiedName()))
					return true;
			}
			return false;
		}

		// 2) Type variables: check "extends" bounds, e.g., <T extends Map<?,?>>
		if (t.isTypeVariable())
		{
			ResolvedTypeVariable tv = t.asTypeVariable();
			ResolvedTypeParameterDeclaration tp = tv.asTypeParameter();
			for (ResolvedTypeParameterDeclaration.Bound b : tp.getBounds())
			{
				if (b.isExtends() && isSubtypeOf(b.getType(), targetFqn))
					return true;
			}
			return false;
		}

		// 3) Wildcards: ? extends X / ? super X / unbounded
		if (t.isWildcard())
		{
			ResolvedWildcard w = t.asWildcard();
			// Older JP: getBoundedType() is a direct value when bounded; use
			// isExtends()/isSuper()/isBounded()
			if (w.isExtends() && w.isBounded())
			{
				ResolvedType bt = w.getBoundedType();
				return isSubtypeOf(bt, targetFqn);
			}
			// '? super X' or unbounded doesn't make the wildcard a subtype of target
			return false;
		}

		// 4) Arrays, primitives, null, unions/intersections (not handled here)
		return false;
	}

	/** True if the resolved type is exactly java.util.Optional<T> for some T. */
	public static boolean isOptionalType(ResolvedType t)
	{
		if (!t.isReferenceType())
			return false;

		ResolvedReferenceType ref = t.asReferenceType();
		// This gives you the full generic qualified name like java.util.Optional
		String qn = ref.getQualifiedName();

		return "java.util.Optional".equals(qn);
	}
	
	 private static String boxedTypeName(ResolvedPrimitiveType prim) {
	    switch (prim) {
	      case BOOLEAN: return "java.lang.Boolean";
	      case BYTE:    return "java.lang.Byte";
	      case SHORT:   return "java.lang.Short";
	      case INT:     return "java.lang.Integer";
	      case LONG:    return "java.lang.Long";
	      case CHAR:    return "java.lang.Character";
	      case FLOAT:   return "java.lang.Float";
	      case DOUBLE:  return "java.lang.Double";
	      default: throw new IllegalArgumentException("Unexpected primitive: " + prim);
	    }
	  }
	
	@SuppressWarnings("deprecation")
	public static boolean isSubtypeOf(String subSig, String superSig, TypeSolver typeSolver) {
    StaticJavaParser.getConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));
		if (subSig.contains("?") || superSig.contains("?") || subSig.compareTo("null") == 0 || superSig.compareTo("null") == 0)
		{
			return false;
		}
    ResolvedType sub = resolveTypeSignature(subSig, typeSolver);
    ResolvedType sup = resolveTypeSignature(superSig, typeSolver);

    if (sup.isAssignableBy(sub)) {
      return true;
    }

    // Handle primitive ↔ boxed
    if (sub.isPrimitive()) {
      ResolvedType boxedSub = resolveTypeSignature(boxedTypeName(sub.asPrimitive()), typeSolver);
      if (sup.isAssignableBy(boxedSub)) return true;
    }
    if (sup.isPrimitive()) {
      ResolvedType boxedSup = resolveTypeSignature(boxedTypeName(sup.asPrimitive()), typeSolver);
      if (boxedSup.isAssignableBy(sub)) return true;
    }

    // Arrays automatically subtype Object, Cloneable, Serializable
    if (sub.isArray() && isArrayTopInterface(sup)) return true;

    return false;
  }

  /** Resolve a Java type signature (FQN, generic, wildcard, array, primitive) to a ResolvedType, with simple caching. */
  private static ResolvedType resolveTypeSignature(String typeSig, TypeSolver ts) {
    // Small cache to avoid reparsing common targets like "java.util.List<? extends Number>"
    return CACHE.computeIfAbsent(typeSig, sig -> {
      // Build a tiny class with one field of the desired type; then resolve that field's type.
      // Using a field (not a local) makes raw types behave like Java (e.g., "List" is raw).
      String src = "class __T { " + sig + " __x; }";
      CompilationUnit cu = StaticJavaParser.parse(src);
      FieldDeclaration fd = cu.findFirst(FieldDeclaration.class)
          .orElseThrow(() -> new IllegalStateException("Failed to parse type signature: " + sig));
      return JavaParserFacade.get(ts).getType(fd.getVariable(0));
    });
  }

  private static boolean isArrayTopInterface(ResolvedType sup) {
    // Per JLS, all array types implement Object, Cloneable, Serializable.
    if (sup.isPrimitive()) return false;
    String qn = sup.describe(); // safe for reference types
    return "java.lang.Object".equals(qn)
        || "java.lang.Cloneable".equals(qn)
        || "java.io.Serializable".equals(qn);
  }

	/** Get the type argument inside Optional<T>, or empty if not an Optional. */
	public static java.util.Optional<ResolvedType> unwrapOptional(ResolvedType t)
	{
		if (!isOptionalType(t))
			return java.util.Optional.empty();
		ResolvedReferenceType ref = t.asReferenceType();
		if (!ref.typeParametersValues().isEmpty())
		{
			return java.util.Optional.of(ref.typeParametersValues().get(0));
		}
		return java.util.Optional.empty();
	}
}
