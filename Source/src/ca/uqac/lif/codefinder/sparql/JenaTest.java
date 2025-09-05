package ca.uqac.lif.codefinder.sparql;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaTest
{
	protected static final String NS = "http://liflab.uqac.ca/";

	private static String prefixes = StrUtils.strjoinNL
			("PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
					"PREFIX lif:  <http://liflab.uqac.ca/>",
					""
					);

	protected static final Property IN = ResourceFactory.createProperty(NS, "in");

	protected static final Property NAME = ResourceFactory.createProperty(NS, "name");

	protected static final Property ARG_1 = ResourceFactory.createProperty(NS, "arg1");

	public static void main(String[] args)
	{
		JavaAstNodeIndex globalAstIndex = new JavaAstNodeIndex();
		
		// Register property function
		PropertyFunctionRegistry.get()
	  .put(NS + "resolvedType",
	       (uri) -> new ResolveType(globalAstIndex));

		// create an empty Model
		Model model = ModelFactory.createDefaultModel();
		{
			Resource cu = model.createResource(NS + "CompilationUnit");
			//cu.getIr
			Resource mc = model.createResource(NS + "MethodCallExpr");
			//globalAstIndex.add("http://liflab.uqac.ca/CompilationUnit", cu);
			cu.addProperty(IN, mc);
			mc.addProperty(NAME, "assert");
			Resource constant = model.createResource(NS + "IntegerLiteralExpr");
			mc.addProperty(ARG_1, constant);
		}

		ResultSet resultSet1 = QueryExecution.model(model)
				.query(prefixes + "SELECT ?x WHERE {   ?x lif:name \"assert\" . ?x lif:resolvedType ?y }").select();
    ResultSetFormatter.out(resultSet1);
	}
}
