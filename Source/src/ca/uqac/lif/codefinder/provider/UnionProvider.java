package ca.uqac.lif.codefinder.provider;

/**
 * Provides a union of multiple {@link FileProvider} sources.
 * Iterates through each provider in sequence, supplying files from each until all are exhausted.
 */
public class UnionProvider implements FileProvider
{
	/** The array of file providers to combine. */
	protected FileProvider[] m_providers;

	/** The index of the current provider being used. */
	protected int m_currentIndex;

	/**
	 * Constructs a UnionProvider from the given file providers.
	 * @param providers The file providers to combine
	 */
	public UnionProvider(FileProvider ... providers)
	{
		super();
		m_providers = providers;
		m_currentIndex = 0;
	}

	/**
	 * Returns {@code true} if there are more files to provide from any of the underlying providers.
	 * @return {@code true} if more files are available, {@code false} otherwise
	 */
	@Override
	public boolean hasNext()
	{
		for (; m_currentIndex < m_providers.length; m_currentIndex++)
		{
			if (m_providers[m_currentIndex].hasNext())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the next file from the current provider, or from the next available provider.
	 * @return The next {@link FileSource}, or {@code null} if none are available
	 */
	@Override
	public FileSource next()
	{
		for (; m_currentIndex < m_providers.length; m_currentIndex++)
		{
			if (m_providers[m_currentIndex].hasNext())
			{
				return m_providers[m_currentIndex].next();
			}
		}
		return null;
	}

	/**
	 * Returns the total number of files provided so far.
	 * @return The number of files provided
	 */
	@Override
	public int filesProvided()
	{
		int files = 0;
		for (FileProvider p : m_providers)
		{
			files += p.filesProvided();
		}
		return files;
	}
}