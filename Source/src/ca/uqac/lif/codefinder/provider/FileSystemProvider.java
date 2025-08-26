package ca.uqac.lif.codefinder.provider;

import java.util.ArrayList;
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
	
	/**
	 * Constructs a FileSystemProvider for the given file system.
	 * @param fs The file system to provide files from
	 * @throws FileSystemException If an error occurs while accessing the file system
	 */
	public FileSystemProvider(FileSystem fs) throws FileSystemException
	{
		super();
		m_fs = fs;
		m_filenames = new ArrayList<String>();
		m_fs.open();
		JavaFileFinder jff = new JavaFileFinder(m_fs);
		jff.crawl();
		m_filenames = jff.getFiles();
		m_iterator = m_filenames.iterator();
	}
	
	/**
	 * Indicates whether there are more files to be provided from the file system.
	 * @return <tt>true</tt> if there are more files, <tt>false</tt> otherwise
	 */
	@Override
	public boolean hasNext()
	{
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
			String filename = m_iterator.next();
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
	 * Returns the number of files provided so far.
	 * @return The number of files provided
	 */
	@Override
	public int filesProvided()
	{
		return m_filenames.size();
	}

}