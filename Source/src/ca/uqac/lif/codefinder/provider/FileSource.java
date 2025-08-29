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

/**
 * Represents a source file to be analyzed, encapsulating its name and input stream.
 */
public class FileSource
{
	/** The name of the file. */
	protected final String m_name;
	
	/** The input stream to read the file's contents. */
	protected final InputStream m_stream;
	
	/**
	 * Constructs a FileSource with the given name and input stream.
	 * @param name The name of the file
	 * @param stream The input stream to read the file's contents
	 */
	public FileSource(String name, InputStream stream)
	{
		super();
		m_name = name;
		m_stream = stream;
	}
	
	@Override
	public String toString()
	{
		return m_name;
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
	 */
	public InputStream getStream()
	{
		return m_stream;
	}
}