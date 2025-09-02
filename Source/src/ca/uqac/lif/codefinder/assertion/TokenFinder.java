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
package ca.uqac.lif.codefinder.assertion;

import java.util.Collection;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public abstract class TokenFinder extends VoidVisitorAdapter<Void>
{
	/** The name of the file to analyze */
	protected final String m_filename;
	
	/** The name of this finder */
	protected final String m_name;
	
	/**
	 * Creates a new token finder.
	 * @param name The name of this finder
	 * @param filename The name of the file to analyze
	 */
	public TokenFinder(String name, String filename)
	{
		super();
		m_filename = filename;
		m_name = name;
	}
	
	/**
	 * Gets the name of this finder.
	 * @return The name of this finder
	 */
	public String getName()
	{
		return m_name;
	}
	
	/**
	 * Creates a new instance of the same type of finder, for a different file.
	 * @param filename The name of the new file
	 * @return A new instance of the same type of finder
	 */
	public abstract TokenFinder newFinder(String filename);
	
	/**
	 * Gets all tokens found by this finder.
	 * @return A collection of found tokens
	 */
	
	/**
	 * Gets all tokens found by this finder.
	 * @return A collection of found tokens. If the method returns
	 * <tt>null</tt>, it means that the finder has only counted the tokens
	 * without storing them.
	 */
	public abstract Collection<FoundToken> getFoundTokens();
	
	/**
	 * Gets the number of tokens found by this finder.
	 * @return The number of tokens found
	 */
	public abstract int getFoundCount();
	
	/**
	 * Trims a string to a certain number of lines. This method
	 * is used to limit the size of code snippets in the output.
	 * @param s The string to trim
	 * @param num_lines The maximum number of lines to keep
	 * @return The trimmed string
	 */
	protected static String trimLines(String s, int num_lines)
	{
		StringBuilder out = new StringBuilder();
		String[] lines = s.split("\\n");
		for (int i = 0; i < Math.min(lines.length, num_lines); i++)
		{
			out.append(lines[i]).append("\n");
		}
		return out.toString();
	}
}	
