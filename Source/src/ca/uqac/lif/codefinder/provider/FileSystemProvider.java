package ca.uqac.lif.codefinder.provider;

import java.util.Iterator;
import java.util.List;

import ca.uqac.lif.fs.FileSystem;
import ca.uqac.lif.fs.FileSystemException;

/**
 * Provides source files from a {@link FileSystem} for analysis.
 * Implements the {@link FileProvider} interface to supply files from a file system abstraction.
 */
public class FileSystemProvider implements FileProvider
{
	/** The file system to read files from. */
	protected final FileSystem m_fs;
	
	/** The list of file names available in the file system. */
	protected List<String> m_filenames;
	
	/** Iterator over the file names. */
	protected Iterator<String> m_iterator;
	
	/** The number of files provided so far. */
	protected int m_provided;
	
	/**
	 * Constructs a FileSystemProvider for the given file system.
	 * @param fs The file system to provide files from
	 * @throws FileSystemException If an error occurs while accessing the file system
	 */
	public FileSystemProvider(FileSystem fs) throws FileSystemException
	{
		super();
		m_provided = 0;
		m_fs = fs;
		m_filenames = null;
		m_iterator = null;
	}
	
	/**
	 * Indicates whether there are more files to be provided from the file system.
	 * @return <tt>true</tt> if there are more files, <tt>false</tt> otherwise
	 */
	@Override
	public boolean hasNext()
	{
		if (m_iterator == null)
		{
			try
			{
				prepare();
				boolean b = m_iterator.hasNext();
				if (!b)
				{
					m_fs.close();
				}
				return b;
			}
			catch (FileSystemException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return m_iterator.hasNext();
	}

	/**
	 * Provides the next file from the file system.
	 * @return The next {@link FileSource} object, or <tt>null</tt> if an error occurs
	 */
	@Override
	public FileSource next()
	{
		try
		{
			if (m_iterator == null)
			{
				prepare();
			}
			String filename = m_iterator.next();
			m_provided++;
			return new FileSource(filename, m_fs.readFrom(filename));
		}
		catch (FileSystemException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Prepares the provider by opening the file system and finding Java files.
	 * @throws FileSystemException If an error occurs while accessing the file system
	 */
	protected void prepare() throws FileSystemException
	{
		m_fs.open();
		JavaFileFinder jff = new JavaFileFinder(m_fs);
		jff.crawl();
		m_filenames = jff.getFiles();
		m_iterator = m_filenames.iterator();
	}

	/**
	 * Returns the number of files provided so far.
	 * @return The number of files provided
	 */
	@Override
	public int filesProvided()
	{
		return m_provided;
	}

}