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
package ca.uqac.lif.piglet.find;

import java.util.HashMap;
import java.util.Map;

import ca.uqac.lif.azrael.ObjectPrinter;
import ca.uqac.lif.azrael.ObjectReader;
import ca.uqac.lif.azrael.PrintException;
import ca.uqac.lif.azrael.Printable;
import ca.uqac.lif.azrael.ReadException;
import ca.uqac.lif.azrael.Readable;

/**
 * Represents a token found in a source file, such as an assertion or code fragment.
 * Stores the filename, start and end line numbers, and an optional code snippet.
 */
public class FoundToken implements Comparable<FoundToken>, Printable, Readable
{
	/** The name of the file where the token was found. */
	protected final String m_filename;
	
	/** The starting line number of the token. */
	protected final int m_startLine;
	
	/** The ending line number of the token. */
	protected final int m_endLine;
	
	/** The code snippet corresponding to the token. */
	protected String m_snippet;
	
	/**
	 * The name of the assertion or token type.
	 */
	protected final String m_name;
	
	/**
	 * Default constructor (for serialization purposes).
	 */
	protected FoundToken()
	{
		m_name = "";
		m_filename = "";
		m_startLine = 0;
		m_endLine = 0;
		m_snippet = "";
	}
	
	/**
	 * Constructs a FoundToken with a code snippet.
	 * @param filename The name of the file
	 * @param start_line The starting line number
	 * @param end_line The ending line number
	 * @param snippet The code snippet
	 */
	public FoundToken(String name, String filename, int start_line, int end_line, String snippet)
	{
		super();
		m_name = name;
		m_filename = filename;
		m_startLine = start_line;
		m_endLine = end_line;
		m_snippet = snippet;
	}
	
	/**
	 * Constructs a FoundToken without a code snippet.
	 * @param filename The name of the file
	 * @param start_line The starting line number
	 * @param end_line The ending line number
	 */
	public FoundToken(String name, String filename, int start_line, int end_line)
	{
		this(name, filename, start_line, end_line, "");
	}
	
	/**
	 * Returns the code snippet for this token.
	 * @return The code snippet
	 */
	public String getSnippet()
	{
		return m_snippet;
	}
	
	/**
	 * Returns a string representation of the token, including filename and line numbers.
	 * @return A string describing the token
	 */
	@Override
	public String toString()
	{
		return m_filename + ": " + getLocation();
	}
	
	/**
	 * Returns the filename where the token was found.
	 * @return The filename
	 */
	public String getFilename()
	{
		return m_filename;
	}
	
	/**
	 * Returns a string representing the location of the token in the file.
	 * @return A string in the format "Lstart-end" or "Lstart" if start and end are the same
	 */
	public String getLocation()
	{
		return "L" + (m_startLine == m_endLine ? m_startLine : m_startLine + "-" + m_endLine);
	}
	
	/**
	 * Returns the name of the assertion or token type.
	 * @return The assertion name
	 */
	public String getAssertionName()
	{
		return m_name;
	}

	/**
	 * Compares this token to another by filename and start line.
	 * @param o The other token
	 * @return A negative integer, zero, or a positive integer as this token is less than, equal to, or greater than the specified token
	 */
	@Override
	public int compareTo(FoundToken o)
	{
		if (o.m_filename.compareTo(m_filename) == 0)
		{
			return m_startLine - o.m_startLine;
		}
		return m_filename.compareTo(o.m_filename);
	}

	@Override
	public FoundToken read(ObjectReader<?> reader, Object o) throws ReadException
	{
		Object map_o = reader.read(o);
		if (!(map_o instanceof Map))
		{
			throw new ReadException("Expected a map, got " + o.getClass().getName());
		}
		@SuppressWarnings("unchecked")
		Map<String,Object> map = (Map<String,Object>) map_o;
		String name = (String) map.get("name");
		String filename = (String) map.get("filename");
		int start_line = ((Number) map.get("start_line")).intValue();
		int end_line = ((Number) map.get("end_line")).intValue();
		String snippet = map.getOrDefault("snippet", "").toString();
		return new FoundToken(name, filename, start_line, end_line, snippet);
	}

	@Override
	public Object print(ObjectPrinter<?> printer) throws PrintException
	{
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("name", m_name);
		map.put("filename", m_filename);
		map.put("start_line", m_startLine);
		map.put("end_line", m_endLine);
		if (m_snippet != null && !m_snippet.isEmpty())
		{
			map.put("snippet", m_snippet);
		}
		return printer.print(map);
	}
}