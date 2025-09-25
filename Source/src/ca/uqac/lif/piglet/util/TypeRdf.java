package ca.uqac.lif.piglet.util;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.resolution.types.*;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.*;
import com.github.javaparser.resolution.model.SymbolReference;
import com.github.javaparser.resolution.model.typesystem.ReferenceTypeImpl;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.resolution.MethodAmbiguityException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Two public APIs: 1) resolveTypeToString(Object symbolOrType, TypeSolver) ->
 * canonical, capture-free string for RDF 2) isSubtypeOf(String subSig, String
 * superSig, TypeSolver) -> generic-aware assignability
 */
public final class TypeRdf
{

	private TypeRdf()
	{
	}

	// Cache for “signature string → ResolvedType” (used when re-resolving strings)
	private static final Map<String, ResolvedType> SIG_RESOLVE_CACHE = new ConcurrentHashMap<>();

	/* ===================== PUBLIC API ===================== */

	/**
	 * Serialize a symbol/type to a stable, wildcard-safe string (suitable for RDF
	 * literals).
	 */
	public static String resolveTypeToString(Object symbolOrType, TypeSolver ts)
	{
		//configure(ts);
		try
		{
			ResolvedType rt = toResolvedType(symbolOrType, ts);
			if (rt == null)
				return "?";
			return canonical(rt);
		}
		catch (UnsolvedSymbolException e)
		{
			return "?";
		}
	}

	/** Generic-aware: returns true iff subSig <: superSig (assignable to). */
	public static boolean isSubtypeOf(String subSig, String superSig, TypeSolver ts)
	{
		//configure(ts);

		try
		{
			subSig = cleanSig(subSig);
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
			if (superPat.args.isEmpty())
			{
				if (sub.isArray() && isArrayTop(superPat.rawFqn))
					return true;

				if (sub.isPrimitive())
				{
					ResolvedType boxed = resolveSignatureToType(boxedTypeName(sub.asPrimitive()), ts);
					return rawAssignable(boxed, superPat.rawFqn, ts);
				}
				return rawAssignable(sub, superPat.rawFqn, ts);
			}

			// SUPER has args: need substituted ancestor of sub matching raw SUPER
			ResolvedReferenceTypeDeclaration superDecl = ts.solveType(superPat.rawFqn);
			ResolvedReferenceType anc = findAncestor(sub, superDecl);
			if (anc == null)
				return false;

			// Actual type arguments after substitution (version-friendly API)
			List<ResolvedType> actuals = anc.typeParametersValues();

			if (actuals.size() != superPat.args.size())
				return false; // arity/raw mismatch

			for (int i = 0; i < actuals.size(); i++)
			{
				if (!argMatches(actuals.get(i), superPat.args.get(i), ts))
					return false;
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
	private static void configure(TypeSolver ts)
	{
		StaticJavaParser.getConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17)
		.setSymbolResolver(new JavaSymbolSolver(ts));
	}

	private static ResolvedType toResolvedType(Object o, TypeSolver ts) {
		if (o instanceof ResolvedType rt) return rt;

		// --- Method calls: most fragile path in JP when generics + wildcards appear ---
		if (o instanceof MethodCallExpr call) {
			// (1) Normal path
			try {
				return JavaParserFacade.get(ts).getType(call);
			} catch (UnsupportedOperationException | IllegalArgumentException | UnsolvedSymbolException | IllegalStateException | MethodAmbiguityException e1) {
				// (2) Usage (does some substitution)
				try {
					var usage = JavaParserFacade.get(ts).solveMethodAsUsage(call);
					return usage.returnType();
				} catch (UnsupportedOperationException | IllegalArgumentException | UnsolvedSymbolException | IllegalStateException | MethodAmbiguityException e2) {
					// (3) Declaration without substitution → then erase generics to avoid further failures
					try {
						SymbolReference<ResolvedMethodDeclaration> ref = JavaParserFacade.get(ts).solve(call);
						if (ref.isSolved()) {
							ResolvedType rt = ref.getCorrespondingDeclaration().getReturnType();
							return eraseToRaw(rt, ts); // drop problematic type args
						}
					} catch (Throwable ignore) { /* fall through */ }
					// (4) Last resort
					return resolveSignatureToType("java.lang.Object", ts);
				}
			}
		}

		// Method references: treat like their functional interface's return type
		if (o instanceof MethodReferenceExpr mref) {
			try {
				return JavaParserFacade.get(ts).getType(mref);
			} catch (Throwable t) {
				return resolveSignatureToType("java.lang.Object", ts);
			}
		}

		// Object creation: usually safe, but keep a guard
		if (o instanceof ObjectCreationExpr newExpr) {
			try {
				return JavaParserFacade.get(ts).getType(newExpr);
			} catch (Throwable t) {
				// Erase to raw of the constructor's type if possible
				try {
					var tpe = newExpr.getType();
					return eraseToRaw(JavaParserFacade.get(ts).convertToUsage(tpe), ts);
				} catch (Throwable t2) {
					return resolveSignatureToType("java.lang.Object", ts);
				}
			}
		}

		if (o instanceof Expression expr) {
			try {
				return JavaParserFacade.get(ts).getType(expr);
			} catch (UnsupportedOperationException | IllegalArgumentException | UnsolvedSymbolException | IllegalStateException | MethodAmbiguityException e) {
				return resolveSignatureToType("java.lang.Object", ts);
			}
		}

		if (o instanceof Type typeAst) {
			try {
				return JavaParserFacade.get(ts).convertToUsage(typeAst);
			} catch (IllegalStateException ise) {
				// “Symbol resolution not configured” case → conservative fallback
				if (typeAst.isVarType()) {
					return resolveSignatureToType("java.lang.Object", ts);
				}
				try {
					return resolveSignatureToType(typeAst.toString(), ts);
				} catch (RuntimeException ex) {
					return resolveSignatureToType("java.lang.Object", ts);
				}
			} catch (UnsupportedOperationException | IllegalArgumentException | UnsolvedSymbolException | MethodAmbiguityException e) {
				return resolveSignatureToType("java.lang.Object", ts);
			}
		}

		if (o instanceof ResolvedValueDeclaration rvd) return rvd.getType();

		if (o instanceof com.github.javaparser.ast.Node node && node instanceof Expression expr2) {
			try {
				return JavaParserFacade.get(ts).getType(expr2);
			} catch (Throwable t) {
				return resolveSignatureToType("java.lang.Object", ts);
			}
		}

		if (o instanceof ClassOrInterfaceDeclaration cid) {
			try {
				ResolvedReferenceTypeDeclaration decl = cid.resolve();
				ResolvedType rt = new ReferenceTypeImpl(decl);
				return rt;
			} catch (Throwable t) {
				return resolveSignatureToType("?", ts);
			}
		}

		throw new IllegalArgumentException("Unsupported input to resolveTypeToString: " +
				(o == null ? "null" : o.getClass().getName()));
	}

	private static ResolvedType eraseToRaw(ResolvedType t, TypeSolver ts) {
		if (t.isArray()) {
			// Erase component then rebuild via our stub
			String erasedComp = TypeRdf.canonical(t.asArrayType().getComponentType());
			return resolveSignatureToType(erasedComp + "[]", ts);
		}
		if (t.isReferenceType()) {
			String qn = t.asReferenceType().getQualifiedName(); // raw FQN
			return resolveSignatureToType(qn, ts);
		}
		return t; // primitive, null, wildcard, etc.
	}

	/* ===================== CANONICALIZATION ===================== */

	// Add these fields near the top of the class:
	private static final int MAX_CANON_DEPTH = 16;
	private static final ThreadLocal<IdentityHashMap<ResolvedType, Boolean>> CANON_STACK = ThreadLocal
			.withInitial(IdentityHashMap::new);

	// Safe canonicalization that avoids infinite recursion.
	private static String canonical(ResolvedType t)
	{
		return canonical(t, 0);
	}

	private static String canonical(ResolvedType t, int depth)
	{
		if (depth > MAX_CANON_DEPTH)
		{
			// Bail out with a conservative rendering
			return erase(t);
		}

		// Cycle guard: if we re-enter with the same type object, stop expanding
		IdentityHashMap<ResolvedType, Boolean> seen = CANON_STACK.get();
		if (seen.put(t, Boolean.TRUE) != null)
		{
			return erase(t);
		}
		try
		{
			if (t.isPrimitive())
				return t.asPrimitive().describe();
			if (t.isNull())
				return "null";
			if (t.isArray())
				return canonical(t.asArrayType().getComponentType(), depth + 1) + "[]";

			if (t.isWildcard())
			{
				ResolvedWildcard w = t.asWildcard();
				if (w.isExtends())
					return "? extends " + canonical(w.getBoundedType(), depth + 1);
				if (w.isSuper())
					return "? super " + canonical(w.getBoundedType(), depth + 1);
				return "?";
			}

			if (t.isTypeVariable())
			{
				// IMPORTANT: do not traverse bounds here (can recurse via convertToUsage()).
				// Prefer the declared name; if not available or looks like a capture, print
				// '?'.
				try
				{
					ResolvedTypeParameterDeclaration tp = t.asTypeVariable().asTypeParameter();
					String n = tp.getName(); // e.g., "T"
					// If you want to be extra-safe about capture-y names, gate them:
					if (n == null || n.isEmpty() || looksLikeCaptureName(n))
						return "?";
					return n;
				}
				catch (Throwable ignored)
				{
					// Not a regular declared type parameter (e.g., a capture) → canonicalize as '?'
					return "?";
				}
			}

			if (t.isReferenceType())
			{
				ResolvedReferenceType ref = t.asReferenceType();
				String base = ref.getQualifiedName();
				List<ResolvedType> params = ref.typeParametersValues(); // version-friendly API
				if (params.isEmpty())
					return base;

				// Render args with depth & cycle guard
				List<String> argStrs = new ArrayList<>(params.size());
				for (ResolvedType p : params)
				{
					argStrs.add(canonical(p, depth + 1));
				}
				return base + "<" + String.join(", ", argStrs) + ">";
			}

			// Fallback
			return t.describe();

		}
		finally
		{
			seen.remove(t);
		}
	}

	// Conservative erasure used when we hit cycles/depth cap
	private static String erase(ResolvedType t)
	{
		if (t.isArray())
			return erase(t.asArrayType().getComponentType()) + "[]";
		if (t.isReferenceType())
			return t.asReferenceType().getQualifiedName();
		if (t.isPrimitive())
			return t.asPrimitive().describe();
		if (t.isWildcard())
			return "?";
		if (t.isTypeVariable())
			return "?";
		return t.describe();
	}

	private static boolean looksLikeCaptureName(String s)
	{
		// Some JP versions produce names like "?CAP#1", "??PP0", etc. Never serialize
		// those.
		return s.startsWith("?") || s.contains("CAP") || s.matches(".*PP\\d+");
	}

	/* ===================== MATCHING / SUBTYPING ===================== */

	private static final class TypePattern
	{
		final String rawFqn;
		final List<ArgPat> args;

		TypePattern(String rawFqn, List<ArgPat> args)
		{
			this.rawFqn = rawFqn;
			this.args = args;
		}

		sealed interface ArgPat
		{
			record Exact(String sig) implements ArgPat
			{
			}

			record Extends(String boundSig) implements ArgPat
			{
			}

			record Super(String boundSig) implements ArgPat
			{
			}

			record Unbounded() implements ArgPat
			{
			}
		}
	}

	private static TypePattern parseSuperPattern(String superSig)
	{
		String trimmed = superSig.trim();
		if (trimmed.equals("?") || trimmed.equals("null"))
			return new TypePattern("java.lang.Object", List.of());

		String src = "class __T { " + superSig + " __x; }";
		CompilationUnit cu = StaticJavaParser.parse(src);
		FieldDeclaration fd = cu.findFirst(FieldDeclaration.class).orElseThrow();
		Type t = fd.getElementType();
		return toPattern(t);
	}

	private static TypePattern toPattern(Type t)
	{
		if (t.isPrimitiveType())
		{
			return new TypePattern(t.asPrimitiveType().toString(), List.of());
		}
		if (t.isArrayType())
		{
			ArrayType at = t.asArrayType();
			TypePattern elem = toPattern(at.getComponentType());
			String raw = elem.rawFqn + "[]".repeat(at.getArrayLevel());
			return new TypePattern(raw, List.of());
		}
		if (t.isClassOrInterfaceType())
		{
			ClassOrInterfaceType ct = t.asClassOrInterfaceType();
			String raw = ct.getNameWithScope(); // expect FQNs ideally
			List<TypePattern.ArgPat> args = new ArrayList<>();
			if (ct.getTypeArguments().isPresent())
			{
				for (Type ta : ct.getTypeArguments().get())
					args.add(toArgPat(ta));
			}
			return new TypePattern(raw, args);
		}
		if (t.isWildcardType())
		{
			return new TypePattern("java.lang.Object", List.of());
		}
		return new TypePattern(t.toString(), List.of());
	}

	private static TypePattern.ArgPat toArgPat(Type ta)
	{
		if (ta.isWildcardType())
		{
			WildcardType w = ta.asWildcardType();
			if (w.getExtendedType().isPresent())
				return new TypePattern.ArgPat.Extends(w.getExtendedType().get().toString());
			if (w.getSuperType().isPresent())
				return new TypePattern.ArgPat.Super(w.getSuperType().get().toString());
			return new TypePattern.ArgPat.Unbounded();
		}
		return new TypePattern.ArgPat.Exact(ta.toString());
	}

	private static boolean argMatches(ResolvedType actual, TypePattern.ArgPat pat, TypeSolver ts)
	{
		if (pat instanceof TypePattern.ArgPat.Unbounded)
			return true;

		if (pat instanceof TypePattern.ArgPat.Exact ex)
		{
			ResolvedType target = resolveSignatureToType(ex.sig(), ts);
			// invariance for generic arguments
			if (target == null || actual == null)
				return false;
			return target.isAssignableBy(actual) && actual.isAssignableBy(target);
		}
		if (pat instanceof TypePattern.ArgPat.Extends ex)
		{
			ResolvedType bound = resolveSignatureToType(ex.boundSig(), ts);
			if (bound == null || actual == null)
				return false;
			// actual <: bound
			return bound.isAssignableBy(actual);
		}
		if (pat instanceof TypePattern.ArgPat.Super su)
		{
			ResolvedType bound = resolveSignatureToType(su.boundSig(), ts);
			if (bound == null || actual == null)
				return false;
			// bound <: actual
			return actual.isAssignableBy(bound);
		}
		return false;
	}

	private static boolean rawAssignable(ResolvedType sub, String superRawFqn, TypeSolver ts)
	{
		if (sub.isReferenceType())
		{
			if (sub.asReferenceType().getQualifiedName().equals(superRawFqn))
				return true;
			ResolvedReferenceTypeDeclaration superDecl = ts.solveType(superRawFqn);
			return findAncestor(sub, superDecl) != null;
		}
		if (sub.isArray())
			return isArrayTop(superRawFqn);
		return false; // primitives handled via boxing by caller
	}

	private static ResolvedReferenceType findAncestor(ResolvedType sub,
			ResolvedReferenceTypeDeclaration superDecl)
	{
		if (!sub.isReferenceType())
			return null;
		for (ResolvedReferenceType anc : sub.asReferenceType().getAllAncestors())
		{
			if (Objects.equals(anc.getQualifiedName(), superDecl.getQualifiedName()))
				return anc;
		}
		return null;
	}

	/* ===================== STRING → ResolvedType ===================== */

	private static ResolvedType resolveSignatureToType(String typeSig, TypeSolver ts)
	{
		return SIG_RESOLVE_CACHE.computeIfAbsent(typeSig, sig -> {
			if (sig == null || sig.isEmpty() || sig.equals("null") || sig.equals("?"))
			{
				return null;
			}
			String src = "class __T { " + sig + " __x; }";
			CompilationUnit cu = StaticJavaParser.parse(src);
			FieldDeclaration fd = cu.findFirst(FieldDeclaration.class).orElseThrow();
			return JavaParserFacade.get(ts).getType(fd.getVariable(0));
		});
	}

	/* ===================== UTILITIES ===================== */

	private static boolean isArrayTop(String rawFqn)
	{
		return "java.lang.Object".equals(rawFqn) || "java.lang.Cloneable".equals(rawFqn)
				|| "java.io.Serializable".equals(rawFqn);
	}

	private static String boxedTypeName(ResolvedPrimitiveType prim)
	{
		return switch (prim)
				{
				case BOOLEAN -> "java.lang.Boolean";
				case BYTE -> "java.lang.Byte";
				case SHORT -> "java.lang.Short";
				case INT -> "java.lang.Integer";
				case LONG -> "java.lang.Long";
				case CHAR -> "java.lang.Character";
				case FLOAT -> "java.lang.Float";
				case DOUBLE -> "java.lang.Double";
				};
	}

	private static String cleanSig(String sig)
	{
		if (sig == null)
			return null;
		sig = sig.trim();
		if ((sig.startsWith("\"") && sig.endsWith("\"")) || (sig.startsWith("'") && sig.endsWith("'")))
		{
			sig = sig.substring(1, sig.length() - 1).trim();
		}
		return sig;
	}
}
