package ca.uqac.lif.codefinder.find;

/**
 * A factory for creating token finders.
 * @param <T> The type of token finder created by this factory
 */
public class TokenFinderFactory
{
	public TokenFinder newFinder()
	{
		// To be overridden in subclasses
		return null;
	}
}
