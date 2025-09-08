package ca.uqac.lif.codefinder.find.sparql;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Model;

import ca.uqac.lif.codefinder.find.FoundToken;
import ca.uqac.lif.codefinder.find.TokenFinder;
import ca.uqac.lif.codefinder.thread.ThreadContext;

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
	protected final String m_filename;
	
	/** The name of this finder */
	protected final String m_name;
	
	/** A Java parser instance */
	protected final ThreadContext m_context;
	
	/**
	 * The SPARQL query to execute.
	 */
	protected final String m_query;
	
	/**
	 * The RDF model to query.
	 */
	protected final Model m_model;
	
	/** The set of found tokens */
	protected Set<FoundToken> m_found;
	
	public SparqlTokenFinder(String name, String filename, String query, Model model)
	{
		this(name, filename, query, model, null);
	}
	
	protected SparqlTokenFinder(String name, String filename, String query, Model model, ThreadContext context)
	{
		super();
		m_filename = filename;
		m_name = name;
		m_model = model;
		m_query = query;
		m_context = context;
		m_found = new HashSet<FoundToken>();
	}
	/**
	 * Creates a new instance of the same type of finder, for a different file.
	 * @param filename The name of the new file
	 * @param context A thread context
	 * @return A new instance of the same type of finder
	 */
	public SparqlTokenFinder newFinder(String filename, Model model, ThreadContext context)
	{
		return new SparqlTokenFinder(m_name, filename, m_query, model, context);
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
		// TODO Auto-generated method stub
		return new HashSet<Throwable>();
	}

	@Override
	public Collection<FoundToken> getFoundTokens()
	{
		return m_found;
	}
}
