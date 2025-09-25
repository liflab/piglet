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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.jena.sparql.util.Context;

import com.github.javaparser.ast.Node;

import ca.uqac.lif.azrael.ObjectPrinter;
import ca.uqac.lif.azrael.ObjectReader;
import ca.uqac.lif.azrael.PrintException;
import ca.uqac.lif.azrael.ReadException;
import ca.uqac.lif.piglet.find.FoundToken;
import ca.uqac.lif.piglet.find.TokenFinder;
import ca.uqac.lif.piglet.find.TokenFinderContext;

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
	protected LazyNodeIndex<Node,String> m_index;

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
	public void addToken(int start, int end, String snippet)
	{
		m_found.add(new FoundToken(m_name, m_filename, start, end, snippet));
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
	public Set<Throwable> getErrors()
	{
		return new HashSet<Throwable>();
	}

	@Override
	public Set<FoundToken> getFoundTokens()
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

	public void setIndex(LazyNodeIndex<Node,String> index)
	{
		m_index = index;
	}

	public void process() throws QueryParseException
	{
		String PF = "http://liflab.uqac.ca/codefinder#resolvedtype";
		String IO = "http://liflab.uqac.ca/codefinder#instanceof";
		QueryExecution qe = QueryExecution.model(m_model).query(prefixes + m_query).build();
		Context ctx = qe.getContext();
		PropertyFunctionRegistry local = PropertyFunctionRegistry.get(ctx);
		local.put(PF, uri -> new ResolveType(m_index, m_context.getTypeSolver()));
		local.put(IO, uri -> new InstanceOf(m_context.getTypeSolver()));
		ctx.set(ARQConstants.registryPropertyFunctions, local);
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
	
	@Override
	public Object print(ObjectPrinter<?> p) throws PrintException
	{
		Map<String,Object> m = new HashMap<>();
		m.put("name", getName());
		m.put("found_tokens", getFoundTokens());
		return p.print(m);
	}

	@SuppressWarnings("unchecked")
	@Override
	public SparqlTokenFinder read(ObjectReader<?> r, Object x) throws ReadException
	{
		Object o = r.read(x);
		if (!(o instanceof Map<?, ?>))
		{
			throw new ReadException("Expected a map, got " + o.getClass().getSimpleName());
		}
		String name = null;
		Set<FoundToken> found_tokens = new HashSet<>();
		Map<String, Object> m = (Map<String, Object>) o;
		if (m.containsKey("name"))
		{
			name = (String) m.get("name");
		}
		else
		{
			throw new ReadException("Missing 'name' entry");
		}
		if (m.containsKey("errors"))
		{
			// Ignore errors for the moments, as exceptions are hard to serialize
		}
		if (m.containsKey("found_tokens"))
		{
			found_tokens.addAll((Set<FoundToken>) m.get("found_tokens"));
		}
		else
		{
			throw new ReadException("Missing 'found_tokens' entry");
		}
		SparqlTokenFinder tf = new SparqlTokenFinder(name, m_query, m_model);
		tf.m_found.addAll(found_tokens);
		return tf;
	}
}
