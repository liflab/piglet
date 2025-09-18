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
package ca.uqac.lif.codefinder.provider;

import java.io.InputStream;

import ca.uqac.lif.fs.FileSystem;
import ca.uqac.lif.fs.FileSystemException;

/**
 * Represents a source file to be analyzed, encapsulating its name and input stream.
 */
public class FileSource
{
	/** The name of the file. */
	protected final String m_name;
	
	/** The file system the file belongs to. */
	protected final FileSystem m_fileSystem;
	
	/** The input stream to read the file's contents. */
	protected InputStream m_stream;
	
	/**
	 * Constructs a FileSource with the given name and input stream.
	 * @param name The name of the file
	 * @param stream The input stream to read the file's contents
	 */
	public FileSource(FileSystem fs, String name)
	{
		super();
		m_fileSystem = fs;
		m_name = name;
		m_stream = null;
	}
	
	@Override
	public String toString()
	{
		return m_name;
	}
	
	/**
	 * Returns the file system the file belongs to.
	 * @return The file system
	 */
	public FileSystem getFileSystem()
	{
		return m_fileSystem;
	}
	
	/**
	 * Returns the name of the file.
	 * @return The file name
	 */
	public String getFilename()
	{
		return m_name;
	}
	
	/**
	 * Returns the input stream for the file's contents.
	 * @return The input stream
	 * @throws FileSystemException 
	 */
	public InputStream getStream() throws FileSystemException
	{
		/*if (m_stream == null)
		{
			m_stream = m_fileSystem.readFrom(m_name);
		}*/
		return m_fileSystem.readFrom(m_name);
		//return m_stream;
	}
}