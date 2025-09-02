import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;

import ca.uqac.lif.codefinder.util.Solvers;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HierarchyProbe {
    @SuppressWarnings("unused")
		public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java HierarchyProbe <sourceRoot>");
            System.exit(1);
        }

        // You can adapt these sets to your CLI/args
        Set<String> sourceRoots = Set.of(args[0]);
        Set<String> jarPaths = Set.of(
        		"/home/sylvain/Workspaces/beepbeep/u-log-generator/Source/Core/lib/petitpoucet-core-2.3.jar"
        		); // add jars if needed

        CombinedTypeSolver ts = Solvers.buildSolver(sourceRoots, null, jarPaths);
        ParserConfiguration pc = Solvers.parserConfig(ts);
        JavaParser jp = new JavaParser(pc);
        JavaParserFacade facade = JavaParserFacade.get(ts);

        List<String> missing = new ArrayList<>();

        for (Path p : listJavaFiles(Paths.get(args[0]))) {
            CompilationUnit cu = jp.parse(p).getResult().orElse(null);
            if (cu == null) continue;

            // extends/implements
            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cls -> {
              for (ClassOrInterfaceType t : cls.getExtendedTypes()) {
                try {
                  ResolvedType rt = t.resolve();                 // <- was ResolvedReferenceType
                  // Touch it to ensure resolution; get FQN when possible
                  String fqn = rt.isReferenceType()
                      ? rt.asReferenceType().getQualifiedName()
                      : rt.describe();
                } catch (RuntimeException ex) {
                  missing.add(msg(p, t, "extends", ex));
                }
              }
              for (ClassOrInterfaceType t : cls.getImplementedTypes()) {
                try {
                  ResolvedType rt = t.resolve();                 // <- was ResolvedReferenceType
                  String fqn = rt.isReferenceType()
                      ? rt.asReferenceType().getQualifiedName()
                      : rt.describe();
                } catch (RuntimeException ex) {
                  missing.add(msg(p, t, "implements", ex));
                }
              }
            });

            // fields
            cu.findAll(FieldDeclaration.class).forEach(f -> {
              try {
                ResolvedType rt = facade.convertToUsage(f.getElementType());
                rt.describe();
              } catch (RuntimeException ex) {
                missing.add(msg(p, f.getElementType(), "field type", ex));
              }
            });

            // fields
            cu.findAll(FieldDeclaration.class).forEach(f -> {
                try {
                    ResolvedType rt = facade.convertToUsage(f.getElementType());
                    rt.describe();
                } catch (RuntimeException ex) {
                    missing.add(msg(p, f.getElementType(), "field type", ex));
                }
            });

            // method returns & params
            cu.findAll(MethodDeclaration.class).forEach(m -> {
                try {
                    ResolvedType rt = facade.convertToUsage(m.getType());
                    rt.describe();
                } catch (RuntimeException ex) {
                    missing.add(msg(p, m.getType(), "method return type", ex));
                }
                m.getParameters().forEach(par -> {
                    try {
                        ResolvedType rt = facade.convertToUsage(par.getType());
                        rt.describe();
                    } catch (RuntimeException ex) {
                        missing.add(msg(p, par.getType(), "param type", ex));
                    }
                });
            });
        }

        if (missing.isEmpty()) {
            System.out.println("All referenced types resolved.");
        } else {
            System.out.println("Unresolved types:");
            missing.stream().distinct().forEach(System.out::println);
        }
    }

    private static List<Path> listJavaFiles(Path root) throws java.io.IOException {
        try (Stream<Path> s = Files.walk(root)) {
            return s.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".java"))
                    .collect(Collectors.toList());
        }
    }

    private static String msg(Path file, com.github.javaparser.ast.Node n, String kind, RuntimeException ex) {
        String where = n.getRange().isPresent() ? n.getRange().get().toString() : "?";
        return file + ":" + where + " [" + kind + "] '" + n + "' -> " + ex.getMessage();
    }
}
