package ca.uqac.lif.codefinder.find.sparql;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;

import ca.uqac.lif.codefinder.find.TokenFinderContext;
import ca.uqac.lif.codefinder.find.ast.PushPopVisitableNode;
import ca.uqac.lif.codefinder.util.Solvers;

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
	
	protected static HashSet<String> s_sourcePaths = new HashSet<>();
	
	protected static HashSet<String> s_jarPaths = new HashSet<>();
	
	protected static String s_root = "";

	public static void main(String[] args) throws Exception
	{
		CTX = ThreadLocal.withInitial(() -> {
			try {
		    CombinedTypeSolver ts = Solvers.buildSolver(s_sourcePaths, s_root, s_jarPaths);
		    
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
		
    ParseResult<CompilationUnit> cu = ctx.getParser().parse("class MyClass {\n"
    		+ "  @Test\n"
    		+ "  public void test1() {\n"
    		+ "    C c = Factory.get();\n"
    		+ "    if (obj != null)\n"
    		+ "      assertEquals(\"foo\", obj.x);\n"
    		+ "  }\n"
    		+ "}");
    if (!cu.isSuccessful())
		{
			System.out.println("Parse error");
			return;
		}
    PushPopVisitableNode n = new PushPopVisitableNode(cu.getResult().get());
    // Build RDF model
    ModelBuilder.ModelBuilderResult r = ModelBuilder.buildModel(n);
    
		JavaAstNodeIndex globalAstIndex = r.getIndex();
		
		// Register property function
		PropertyFunctionRegistry.get()
	  .put(NS + "resolvedType",
	       (uri) -> new ResolveType(globalAstIndex));
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		RDFDataMgr.write(baos, r.getModel(), Lang.RDFXML);
		System.out.println(baos.toString());

		/*ResultSet resultSet1 = QueryExecution.model(r.getModel())
				.query(prefixes + "SELECT ?name WHERE {   ?x lif:name \"assertTrue\" . ?x lif:args ?z . ?z lif:in ?y . ?y lif:nodetype \"NameExpr\" . ?y lif:name ?name }").select();
    ResultSetFormatter.out(resultSet1);*/
	}
}
