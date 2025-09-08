package ca.uqac.lif.codefinder.find.sparql;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Model;

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
	
	public static abstract class SparqlTokenFinderFactory extends TokenFinderFactory
	{
		@Override
		public abstract SparqlTokenFinder newFinder();
	}
	
}
