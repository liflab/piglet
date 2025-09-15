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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.jena.sparql.util.Context;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;

import ca.uqac.lif.codefinder.find.FoundToken;
import ca.uqac.lif.codefinder.find.TokenFinderContext;
import ca.uqac.lif.codefinder.find.TokenFinderFactory;
import ca.uqac.lif.codefinder.find.TokenFinder;

/**
 * A token finder that executes a SPARQL query on an RDF model.
 * The RDF model is built from the AST of a source code file. This is
 * meant to be an alternate approach to the usual AST-based token
 * finders.
 * @author Sylvain Hallé
 */
public class SparqlTokenFinder implements TokenFinder
{
	/** The name of the file to analyze */
	protected String m_filename;

	/** The name of this finder */
	protected final String m_name;

	/** A Java parser instance */
	protected TokenFinderContext m_context;

	/** An index of AST nodes */
	protected LazyNodeIndex<Expression,String> m_index;

	public static final String prefixes = StrUtils.strjoinNL
			("PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
					"PREFIX :  <http://liflab.uqac.ca/codefinder#>",
					""
					);

	/**
	 * The SPARQL query to execute.
	 */
	protected final String m_query;

	/**
	 * The RDF model to query.
	 */
	protected Model m_model;

	/** The set of found tokens */
	protected Set<FoundToken> m_found;

	public SparqlTokenFinder(String name, String query, Model model)
	{
		super();
		m_filename = null;
		m_name = name;
		m_model = model;
		m_query = query;
		m_context = null;
		m_found = new HashSet<FoundToken>();
	}

	public SparqlTokenFinder(String name, String query)
	{
		this(name, query, null);
	}

	public void setModel(Model model)
	{
		m_model = model;
	}

	@Override
	public String getName()
	{
		return m_name;
	}

	@Override
	public int getFoundCount()
	{
		return m_found.size();
	}

	@Override
	public Collection<Throwable> getErrors()
	{
		return new HashSet<Throwable>();
	}

	@Override
	public Collection<FoundToken> getFoundTokens()
	{
		return m_found;
	}

	@Override
	public void setContext(TokenFinderContext context)
	{
		m_context = context;
	}

	@Override
	public void setFilename(String filename)
	{
		m_filename = filename;
	}

	public void setIndex(LazyNodeIndex<Expression,String> index)
	{
		m_index = index;
	}

	public void process()
	{

		String PF = "http://liflab.uqac.ca/resolvedtype";
		QueryExecution qe = QueryExecution.model(m_model).query(prefixes + m_query).build();
		Context ctx = qe.getContext();
		PropertyFunctionRegistry local = PropertyFunctionRegistry.get(ctx);
		local.put(PF, uri -> new ResolveType(m_index, m_context.getTypeSolver()));
		ctx.set(ARQConstants.registryPropertyFunctions, local);
		// Register property function
		PropertyFunctionRegistry.get()
		.put("http://liflab.uqac.ca/resolvedtype",
				(uri) -> new ResolveType(m_index, m_context.getTypeSolver()));
		ResultSet resultSet1 = qe.execSelect();
		while (resultSet1.hasNext())
		{
			QuerySolution soln = resultSet1.next();
			Resource n = soln.getResource("n");
			String iri = n.getURI();
			if (iri == null)
			{
				continue;
			}
			Node ast_node = m_index.get(iri);
			if (ast_node == null)
			{
				continue;
			}
			FoundToken t = new FoundToken(m_name, m_filename, ast_node.getRange().get().begin.line, ast_node.getRange().get().end.line, ast_node.toString());
			m_found.add(t);
		}
	}

	public static class SparqlTokenFinderFactory extends TokenFinderFactory
	{
		/**
		 * The name of this finder
		 */
		protected final String m_name;

		/**
		 * The SPARQL query to execute.
		 */
		protected final String m_query;

		public SparqlTokenFinderFactory(String name, String query)
		{
			super();
			m_name = name;
			m_query = query;
		}

		@Override
		public SparqlTokenFinder newFinder()
		{
			return new SparqlTokenFinder(m_name, m_query);
		}
	}
}
