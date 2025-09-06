package ca.uqac.lif.codefinder.find.sparql;

import java.util.Collection;

import org.apache.jena.rdf.model.Model;

import ca.uqac.lif.codefinder.find.FoundToken;
import ca.uqac.lif.codefinder.find.TokenFinder;
import ca.uqac.lif.codefinder.find.ast.AstTokenFinder;
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
	/**
	 * The SPARQL query to execute.
	 */
	protected final String m_query;
	
	/**
	 * The RDF model to query.
	 */
	protected final Model m_model;
	
	public SparqlTokenFinder(String query, Model model)
	{
		super();
		m_model = model;
		m_query = query;
	}
	
	@Override
	public AstTokenFinder newFinder(String filename, ThreadContext context)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getFoundCount()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<Throwable> getErrors()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<FoundToken> getFoundTokens()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
