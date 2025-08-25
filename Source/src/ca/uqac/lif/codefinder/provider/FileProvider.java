package ca.uqac.lif.codefinder.provider;

/**
 * An interface for objects that provide source files to be analyzed.
 * Implementations of this interface typically read files from a file system,
 * a compressed archive, or a version control repository.
 * 
 * @author Sylvain Hallé
 */
public interface FileProvider
{
	/**
	 * Indicates whether there are more files to be provided.
	 * @return <tt>true</tt> if there are more files, <tt>false</tt> otherwise
	 */
	public boolean hasNext();
	
	/**
	 * Provides the next file.
	 * @return The next file, or <tt>null</tt> if there are no more files
	 */
	public FileSource next();
	
	/**
	 * Returns the number of files that have been provided so far.
	 * @return The number of files provided
	 */
	public int filesProvided();
}
