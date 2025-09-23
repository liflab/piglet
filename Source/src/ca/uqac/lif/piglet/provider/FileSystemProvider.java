/*
    Analysis of assertions in Java programs
    Copyright (C) 2025 Sylvain Hallé, Sarika Machhindra Kadam

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.piglet.provider;

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
	 * @return The next {@link FileSource} object
	 */
	@Override
	public FileSource next()
	{
		String filename = m_iterator.next();
		return new FileSource(m_fs, filename);
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