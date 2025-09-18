package ca.uqac.lif.codefinder.find;

import java.util.Collection;

import com.github.javaparser.ast.Node;

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
	public Collection<Throwable> getErrors();
	
	/**
	 * Gets all tokens found by this finder.
	 * @return A collection of found tokens. If the method returns
	 * <tt>null</tt>, it means that the finder has only counted the tokens
	 * without storing them.
	 */
	public Collection<FoundToken> getFoundTokens();
	
	/**
	 * Sets the thread context in which this finder operates.
	 * @param context The thread context
	 */
	public void setContext(TokenFinderContext context);
	
	/**
	 * Sets the name of the file to analyze. This is used to
	 * put inside the found tokens.
	 * @param filename The name of the file to analyze
	 */
	public void setFilename(String filename);
	
	/**
	 * Adds a found token based on a given AST node.
	 * 
	 * @param n
	 *          The AST node that represents the found token
	 */
	public default void addToken(Node n)
	{
		addToken(n.getBegin().get().line, n.getEnd().get().line, n.toString());
	}
	
	/**
	 * Adds a found token based on character offsets.
	 * 
	 * @param start
	 *          The start offset of the found token
	 * @param end
	 *          The end offset of the found token
	 * @param snippet
	 *          A snippet of code corresponding to the found token
	 */
	public void addToken(int start, int end, String snippet);
}
