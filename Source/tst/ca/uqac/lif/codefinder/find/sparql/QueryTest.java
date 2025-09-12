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
package ca.uqac.lif.codefinder.find.sparql;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;

import ca.uqac.lif.codefinder.find.ast.PushPopVisitableNode;
import ca.uqac.lif.codefinder.find.sparql.ModelBuilder.ModelBuilderResult;
import ca.uqac.lif.codefinder.util.Solvers;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.FileUtils;

/**
 * Unit tests for graph querying with SPARQL.
 */
public class QueryTest
{
	/** Type solver shared by all threads */
	private CombinedTypeSolver ts;

	/** Type solver for THIS thread */
	private ParserConfiguration threadPc;

	/** Java parser for THIS thread */
	private JavaParser parser;

	/** Common prefixes for SPARQL queries */
	private static final String prefixes = 
			"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
					"PREFIX lif:  <" + ModelBuilder.NS + ">\n" +
					"\n";

	/**
	 * Creates a new test class.
	 */
	public QueryTest()
	{
		try
		{
			ts = Solvers.buildSolver(new HashSet<>(), "", new HashSet<>());
			threadPc = new ParserConfiguration()
					.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_11)
					.setSymbolResolver(new com.github.javaparser.symbolsolver.JavaSymbolSolver(ts));
			parser = new JavaParser(threadPc);
		}
		catch (FileSystemException | IOException e)
		{
			// TODO Auto-generated catch block
			ts = null;
			e.printStackTrace();
		}
	}

	@Test
	public void test1() throws FileSystemException
	{
		ModelBuilderResult res = readCode("MyClass1.java.src");
		String q = prefixes + """
				SELECT ?n WHERE {
				?n lif:nodetype "MethodDeclaration" .
				?n lif:javadoc ?x
				}
				""";
		ResultSet rs = QueryExecution.model(res.getModel())
				.query(q).select();
		assertTrue(rs.hasNext());
	}

	@Test
	public void test2() throws FileSystemException
	{
		ModelBuilderResult res = readCode("MyClass2.java.src");
		String q = prefixes + """
				SELECT ?n WHERE {
				?n lif:nodetype "MethodDeclaration" .
				?n lif:javadoc ?x
				}
				""";
		ResultSet rs = QueryExecution.model(res.getModel())
				.query(q).select();
		assertFalse(rs.hasNext());
	}

	@Test
	public void test3() throws FileSystemException
	{
		ModelBuilderResult res = readCode("MyClass1.java.src");
		String q = prefixes + """
				SELECT ?n WHERE {
				?c lif:nodetype "CompilationUnit" .
				?c lif:in+ ?n .
				?n lif:nodetype "MethodDeclaration"
				}
				""";
		ResultSet rs = QueryExecution.model(res.getModel())
				.query(q).select();
		assertTrue(rs.hasNext());
	}

	@Test
	public void test4() throws FileSystemException
	{
		ModelBuilderResult res = readCode("MyClass2.java.src");
		String q = prefixes + """
				SELECT ?n WHERE {
				?c lif:nodetype "CompilationUnit" .
				?c lif:in+ ?n .
				?n lif:nodetype "MethodCallExpr"
				}
				""";
		ResultSet rs = QueryExecution.model(res.getModel())
				.query(q).select();
		assertTrue(rs.hasNext());
	}

	@Test
	public void test5() throws FileSystemException
	{
		ModelBuilderResult res = readCode("MyClass2.java.src");
		String q = prefixes + """
				SELECT ?n WHERE {
				?n lif:resolvedtype "int"
				}
				""";
		// Register property function
		PropertyFunctionRegistry.get()
		.put("http://liflab.uqac.ca/resolvedtype",
				(uri) -> new ResolveType(res.getIndex(), ts));
		ResultSet rs = QueryExecution.model(res.getModel())
				.query(q).select();
		assertTrue(rs.hasNext());
	}

	/**
	 * Prints the RDF model to standard output in RDF/XML format.
	 * @param r The model to print
	 */
	protected void printModel(ModelBuilderResult r)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		RDFDataMgr.write(baos, r.getModel(), Lang.RDFXML);
		System.out.println(baos.toString());
	}

	/**
	 * Reads the content of a resource file and builds its RDF model.
	 * @param path The path to the resource file
	 * @return The model
	 * @throws FileSystemException If the file cannot be read
	 */
	protected ModelBuilderResult readCode(String path) throws FileSystemException
	{
		String code = new String(FileUtils.toBytes(QueryTest.class.getResourceAsStream(path)));
		ParseResult<CompilationUnit> res = parser.parse(code);
		PushPopVisitableNode ppn = new PushPopVisitableNode(res.getResult().get());
		ModelBuilderResult r = ModelBuilder.buildModel(ppn);
		return r;
	}
}
