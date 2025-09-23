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
package ca.uqac.lif.piglet.find.sparql;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;

import ca.uqac.lif.piglet.find.TokenFinderContext;
import ca.uqac.lif.piglet.find.visitor.PushPopVisitableNode;
import ca.uqac.lif.piglet.util.Solvers;

public class JenaTest
{
	protected static final String NS = "http://liflab.uqac.ca/";

	public static final String prefixes = StrUtils.strjoinNL
			("PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
					"PREFIX lif:  <http://liflab.uqac.ca/>",
					""
					);

	protected static final Property IN = ResourceFactory.createProperty(NS, "in");

	protected static final Property NAME = ResourceFactory.createProperty(NS, "name");

	protected static final Property ARG_1 = ResourceFactory.createProperty(NS, "arg1");
	
	public static ThreadLocal<TokenFinderContext> CTX;
	
	protected static ArrayList<String> s_sourcePaths = new ArrayList<>();
	
	protected static HashSet<String> s_jarPaths = new HashSet<>();
	
	protected static String s_root = "";

	public static void main(String[] args) throws Exception
	{
		s_sourcePaths.add("/home/sylvain/AssertionStudy/Repositories/thun");
		s_root = "mypackage";
		CTX = ThreadLocal.withInitial(() -> {
			try {
		    CombinedTypeSolver ts = Solvers.buildSolver(s_sourcePaths, new String[] {s_root}, s_jarPaths);
		    
		    // Wire parser to THIS thread’s solver
		    ParserConfiguration threadPc = new ParserConfiguration()
		        .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_11)
		        .setSymbolResolver(new com.github.javaparser.symbolsolver.JavaSymbolSolver(ts));

		    return new TokenFinderContext(
		        ts,
		        new JavaParser(threadPc),
		        com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade.get(ts),
		        100
		    );
		  } catch (Exception e) {
		    throw new RuntimeException("Failed to init per-thread context", e);
		  }
		});
		
		TokenFinderContext ctx = CTX.get();
		
    /*ParseResult<CompilationUnit> cu = ctx.getParser().parse("class MyClass {\n"
    		+ "  @Test\n"
    		+ "  public void test1() {\n"
    		+ "    C c = Factory.get();\n"
    		+ "    if (obj != null)\n"
    		+ "      assertEquals(\"foo\", obj.x);\n"
    		+ "  }\n"
    		+ "}");*/
		FileInputStream fis = new FileInputStream("/home/sylvain/AssertionStudy/Repositories/thun/LinearLayoutManager.java");
		ParseResult<CompilationUnit> cu = ctx.getParser().parse(fis);
		fis.close();
    if (!cu.isSuccessful())
		{
			System.out.println("Parse error");
			return;
		}
    PushPopVisitableNode n = new PushPopVisitableNode(cu.getResult().get());
    // Build RDF model
    ModelBuilder.ModelBuilderResult r = ModelBuilder.buildModel(n, 2, ctx);
    
    LazyNodeIndex<Node,String> globalAstIndex = r.getIndex();
		
		// Register property function
		PropertyFunctionRegistry.get()
	  .put(NS + "resolvedType",
	       (uri) -> new ResolveType(globalAstIndex, ctx.getTypeSolver()));
		
		FileOutputStream fos = new FileOutputStream("/tmp/model.dot");
		RdfRenderer renderer = new RdfRenderer(r.getModel());
		renderer.toDot(new PrintStream(fos));
		fos.close();;

		/*
		ResultSet resultSet1 = QueryExecution.model(r.getModel())
				//.query(prefixes + "SELECT ?name WHERE {   ?x lif:name \"assertTrue\" . ?x lif:args ?z . ?z lif:in ?y . ?y lif:nodetype \"NameExpr\" . ?y lif:name ?name }").select();
				.query(prefixes + "SELECT ?n (GROUP_CONCAT(DISTINCT ?t;separator=\",\") AS ?types)\n"
						+ "           (GROUP_CONCAT(DISTINCT ?nm;separator=\",\") AS ?names)\n"
						+ "WHERE {\n"
						+ "  ?n lif:nodetype ?t ;\n"
						+ "     lif:name     ?nm .\n"
						+ "}\n"
						+ "GROUP BY ?n\n"
						+ "HAVING (COUNT(DISTINCT ?t) > 1 || COUNT(DISTINCT ?nm) > 1)\n"
						+ "LIMIT 20").select();
    ResultSetFormatter.out(resultSet1);
    */
	}
}
