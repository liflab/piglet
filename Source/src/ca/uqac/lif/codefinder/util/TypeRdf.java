package ca.uqac.lif.codefinder.util;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;

import com.github.javaparser.resolution.types.*;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.*;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Two public APIs:
 *  1) resolveTypeToString(Object symbolOrType, TypeSolver) -> canonical, capture-free string for RDF
 *  2) isSubtypeOf(String subSig, String superSig, TypeSolver) -> generic-aware assignability
 */
public final class TypeRdf {

	private TypeRdf() {}

	// Cache for “signature string → ResolvedType” (used when re-resolving strings)
	private static final Map<String, ResolvedType> SIG_RESOLVE_CACHE = new ConcurrentHashMap<>();

	/* ===================== PUBLIC API ===================== */

	/** Serialize a symbol/type to a stable, wildcard-safe string (suitable for RDF literals). */
	public static String resolveTypeToString(Object symbolOrType, TypeSolver ts) {
		configure(ts);
		try
		{
			ResolvedType rt = toResolvedType(symbolOrType, ts);
			return canonical(rt);
		}
		catch (UnsolvedSymbolException e)
		{
			return "?";
		}
	}

	/** Generic-aware: returns true iff subSig <: superSig (assignable to). */
	public static boolean isSubtypeOf(String subSig, String superSig, TypeSolver ts) {
		configure(ts);

		try
		{
			subSig   = cleanSig(subSig);
			superSig = cleanSig(superSig);

			if (subSig.contains("?") || superSig.contains("?"))
			{
				// We don't handle wildcards in the input signatures
				return false;
			}

			// Parse SUPER into an AST pattern to preserve wildcards literally
			TypePattern superPat = parseSuperPattern(superSig);

			// Resolve SUB into a ResolvedType (can include generics)
			ResolvedType sub = resolveSignatureToType(subSig, ts);

			// Fast paths when SUPER is raw (no args)
			if (superPat.args.isEmpty()) {
				if (sub.isArray() && isArrayTop(superPat.rawFqn)) return true;

				if (sub.isPrimitive()) {
					ResolvedType boxed = resolveSignatureToType(boxedTypeName(sub.asPrimitive()), ts);
					return rawAssignable(boxed, superPat.rawFqn, ts);
				}
				return rawAssignable(sub, superPat.rawFqn, ts);
			}

			// SUPER has args: need substituted ancestor of sub matching raw SUPER
			ResolvedReferenceTypeDeclaration superDecl = ts.solveType(superPat.rawFqn);
			ResolvedReferenceType anc = findAncestor(sub, superDecl);
			if (anc == null) return false;

			// Actual type arguments after substitution (version-friendly API)
			List<ResolvedType> actuals = anc.typeParametersValues();

			if (actuals.size() != superPat.args.size()) return false; // arity/raw mismatch

			for (int i = 0; i < actuals.size(); i++) {
				if (!argMatches(actuals.get(i), superPat.args.get(i), ts)) return false;
			}
			return true;
		}
		catch (UnsolvedSymbolException e)
		{
			return false;
		}
	}

	/* ===================== RESOLUTION ===================== */

	@SuppressWarnings("deprecation")
	private static void configure(TypeSolver ts) {
		StaticJavaParser.getConfiguration()
		.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17)
		.setSymbolResolver(new JavaSymbolSolver(ts));
	}

	private static ResolvedType toResolvedType(Object o, TypeSolver ts) throws UnsolvedSymbolException {
		if (o instanceof ResolvedType rt) return rt;
		if (o instanceof Expression expr) return JavaParserFacade.get(ts).getType(expr);
		if (o instanceof Type typeAst)    return JavaParserFacade.get(ts).convertToUsage(typeAst);
		if (o instanceof ResolvedValueDeclaration rvd) return rvd.getType();
		if (o instanceof Node node && node instanceof Expression expr) {
			return JavaParserFacade.get(ts).getType(expr);
		}
		throw new IllegalArgumentException("Unsupported input to resolveTypeToString: " +
				(o == null ? "null" : o.getClass().getName()));
	}

	/* ===================== CANONICALIZATION ===================== */

	/**
	 * Stable, capture-free serialization:
	 * - FQNs for reference types
	 * - Recurse into type args
	 * - Wildcards as ?, ? extends T, ? super T
	 * - Type variables rendered via bounds (no capture names)
	 */
	private static String canonical(ResolvedType t) {
		if (t.isPrimitive()) return t.asPrimitive().describe();
		if (t.isNull()) return "null";
		if (t.isArray()) return canonical(t.asArrayType().getComponentType()) + "[]";

		if (t.isWildcard()) {
			ResolvedWildcard w = t.asWildcard();
			if (w.isExtends()) return "? extends " + canonical(w.getBoundedType());
			if (w.isSuper())   return "? super "   + canonical(w.getBoundedType());
			return "?";
		}

		if (t.isTypeVariable()) {
			// Version-friendly: get bounds from the declaring type parameter
			ResolvedTypeVariable tv = t.asTypeVariable();
			ResolvedTypeParameterDeclaration tp = tv.asTypeParameter();
			// Java type parameters only have *extends* bounds. If none: Object by default.
			if (!tp.getBounds().isEmpty()) {
				// If multiple bounds A & B, Java means "A & B". We render the first as upper bound.
				// (You could join with " & " if you want full fidelity.)
				ResolvedType bound = tp.getBounds().get(0).getType();
				return "? extends " + canonical(bound);
			}
			return "?"; // unbounded type var
		}

		if (t.isReferenceType()) {
			ResolvedReferenceType ref = t.asReferenceType();
			String base = ref.getQualifiedName();
			List<String> args = ref.typeParametersValues()
					.stream().map(TypeRdf::canonical).collect(Collectors.toList());
			return args.isEmpty() ? base : base + "<" + String.join(", ", args) + ">";
		}

		// Fallback
		return t.describe();
	}

	/* ===================== MATCHING / SUBTYPING ===================== */

	private static final class TypePattern {
		final String rawFqn;
		final List<ArgPat> args;
		TypePattern(String rawFqn, List<ArgPat> args) { this.rawFqn = rawFqn; this.args = args; }

		sealed interface ArgPat {
			record Exact(String sig) implements ArgPat {}
			record Extends(String boundSig) implements ArgPat {}
			record Super(String boundSig) implements ArgPat {}
			record Unbounded() implements ArgPat {}
		}
	}

	private static TypePattern parseSuperPattern(String superSig) {
		String trimmed = superSig.trim();
		if (trimmed.equals("?")) return new TypePattern("java.lang.Object", List.of());

		String src = "class __T { " + superSig + " __x; }";
		CompilationUnit cu = StaticJavaParser.parse(src);
		FieldDeclaration fd = cu.findFirst(FieldDeclaration.class).orElseThrow();
		Type t = fd.getElementType();
		return toPattern(t);
	}

	private static TypePattern toPattern(Type t) {
		if (t.isPrimitiveType()) {
			return new TypePattern(t.asPrimitiveType().toString(), List.of());
		}
		if (t.isArrayType()) {
			ArrayType at = t.asArrayType();
			TypePattern elem = toPattern(at.getComponentType());
			String raw = elem.rawFqn + "[]".repeat(at.getArrayLevel());
			return new TypePattern(raw, List.of());
		}
		if (t.isClassOrInterfaceType()) {
			ClassOrInterfaceType ct = t.asClassOrInterfaceType();
			String raw = ct.getNameWithScope(); // expect FQNs ideally
			List<TypePattern.ArgPat> args = new ArrayList<>();
			if (ct.getTypeArguments().isPresent()) {
				for (Type ta : ct.getTypeArguments().get()) args.add(toArgPat(ta));
			}
			return new TypePattern(raw, args);
		}
		if (t.isWildcardType()) {
			return new TypePattern("java.lang.Object", List.of());
		}
		return new TypePattern(t.toString(), List.of());
	}

	private static TypePattern.ArgPat toArgPat(Type ta) {
		if (ta.isWildcardType()) {
			WildcardType w = ta.asWildcardType();
			if (w.getExtendedType().isPresent())
				return new TypePattern.ArgPat.Extends(w.getExtendedType().get().toString());
			if (w.getSuperType().isPresent())
				return new TypePattern.ArgPat.Super(w.getSuperType().get().toString());
			return new TypePattern.ArgPat.Unbounded();
		}
		return new TypePattern.ArgPat.Exact(ta.toString());
	}

	private static boolean argMatches(ResolvedType actual, TypePattern.ArgPat pat, TypeSolver ts) {
		if (pat instanceof TypePattern.ArgPat.Unbounded) return true;

		if (pat instanceof TypePattern.ArgPat.Exact ex) {
			ResolvedType target = resolveSignatureToType(ex.sig(), ts);
			// invariance for generic arguments
			return target.isAssignableBy(actual) && actual.isAssignableBy(target);
		}
		if (pat instanceof TypePattern.ArgPat.Extends ex) {
			ResolvedType bound = resolveSignatureToType(ex.boundSig(), ts);
			// actual <: bound
			return bound.isAssignableBy(actual);
		}
		if (pat instanceof TypePattern.ArgPat.Super su) {
			ResolvedType bound = resolveSignatureToType(su.boundSig(), ts);
			// bound <: actual
			return actual.isAssignableBy(bound);
		}
		return false;
	}

	private static boolean rawAssignable(ResolvedType sub, String superRawFqn, TypeSolver ts) {
		if (sub.isReferenceType()) {
			if (sub.asReferenceType().getQualifiedName().equals(superRawFqn)) return true;
			ResolvedReferenceTypeDeclaration superDecl = ts.solveType(superRawFqn);
			return findAncestor(sub, superDecl) != null;
		}
		if (sub.isArray()) return isArrayTop(superRawFqn);
		return false; // primitives handled via boxing by caller
	}

	private static ResolvedReferenceType findAncestor(ResolvedType sub, ResolvedReferenceTypeDeclaration superDecl) {
		if (!sub.isReferenceType()) return null;
		for (ResolvedReferenceType anc : sub.asReferenceType().getAllAncestors()) {
			if (Objects.equals(anc.getQualifiedName(), superDecl.getQualifiedName())) return anc;
		}
		return null;
	}

	/* ===================== STRING → ResolvedType ===================== */

	private static ResolvedType resolveSignatureToType(String typeSig, TypeSolver ts) {
		return SIG_RESOLVE_CACHE.computeIfAbsent(typeSig, sig -> {
			String src = "class __T { " + sig + " __x; }";
			CompilationUnit cu = StaticJavaParser.parse(src);
			FieldDeclaration fd = cu.findFirst(FieldDeclaration.class).orElseThrow();
			return JavaParserFacade.get(ts).getType(fd.getVariable(0));
		});
	}

	/* ===================== UTILITIES ===================== */

	private static boolean isArrayTop(String rawFqn) {
		return "java.lang.Object".equals(rawFqn)
				|| "java.lang.Cloneable".equals(rawFqn)
				|| "java.io.Serializable".equals(rawFqn);
	}

	private static String boxedTypeName(ResolvedPrimitiveType prim) {
		return switch (prim) {
		case BOOLEAN -> "java.lang.Boolean";
		case BYTE    -> "java.lang.Byte";
		case SHORT   -> "java.lang.Short";
		case INT     -> "java.lang.Integer";
		case LONG    -> "java.lang.Long";
		case CHAR    -> "java.lang.Character";
		case FLOAT   -> "java.lang.Float";
		case DOUBLE  -> "java.lang.Double";
		};
	}

	private static String cleanSig(String sig) {
		if (sig == null) return null;
		sig = sig.trim();
		if ((sig.startsWith("\"") && sig.endsWith("\"")) ||
				(sig.startsWith("'") && sig.endsWith("'"))) {
			sig = sig.substring(1, sig.length() - 1).trim();
		}
		return sig;
	}
}
