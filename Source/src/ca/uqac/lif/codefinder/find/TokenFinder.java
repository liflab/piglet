package ca.uqac.lif.codefinder.find;

import java.util.Collection;

public interface TokenFinder
{
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
