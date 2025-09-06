package ca.uqac.lif.codefinder.find;

import java.util.Collection;

import ca.uqac.lif.codefinder.find.ast.AstTokenFinder;
import ca.uqac.lif.codefinder.thread.ThreadContext;

public interface TokenFinder
{
	/**
	 * Creates a new instance of the same type of finder, for a different file.
	 * @param filename The name of the new file
	 * @param context A thread context
	 * @return A new instance of the same type of finder
	 */
	public AstTokenFinder newFinder(String filename, ThreadContext context);
	
	/**
	 * Gets the name of this finder.
	 * @return The name of this finder
	 */
	public String getName();
	
	/**
	 * Gets the number of tokens found by this finder.
	 * @return The number of tokens found
	 */
	public int getFoundCount();
	
	/**
	 * Gets the errors encountered during the analysis.
	 * @return A collection of errors encountered during the analysis
	 */
	public abstract Collection<Throwable> getErrors();
	
	/**
	 * Gets all tokens found by this finder.
	 * @return A collection of found tokens. If the method returns
	 * <tt>null</tt>, it means that the finder has only counted the tokens
	 * without storing them.
	 */
	public abstract Collection<FoundToken> getFoundTokens();
}
