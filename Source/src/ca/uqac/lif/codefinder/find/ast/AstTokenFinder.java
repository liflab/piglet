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
package ca.uqac.lif.codefinder.find.ast;

import ca.uqac.lif.codefinder.find.TokenFinderContext;
import ca.uqac.lif.codefinder.find.TokenFinder;

/**
 * An abstract base class for token finders that analyze the AST of a
 * Java source code file.
 */
public abstract class AstTokenFinder extends PushPopVisitorAdapter implements TokenFinder
{
	/** The name of the file to analyze */
	protected String m_filename;
	
	/** The name of this finder */
	protected final String m_name;
	
	/** A Java parser instance */
	protected TokenFinderContext m_context;
	
	/**
	 * Creates a new token finder.
	 * @param name The name of this finder
	 * @param filename The name of the file to analyze
	 */
	public AstTokenFinder(String name)
	{
		this(name, null);
	}
	
	/**
	 * Creates a new token finder.
	 * @param name The name of this finder
	 * @param filename The name of the file to analyze
	 * @param context A thread context
	 */
	protected AstTokenFinder(String name, TokenFinderContext context)
	{
		super();
		m_name = name;
		m_context = context;
	}
	
	@Override
	public void setContext(TokenFinderContext context)
	{
		m_context = context;
	}
	
	@Override
	public void setFilename(String filename)
	{
		m_filename = filename;
	}
	
	@Override
	public String getName()
	{
		return m_name;
	}
	
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
