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
package ca.uqac.lif.codefinder.find.visitor;

import ca.uqac.lif.codefinder.find.TokenFinderContext;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import ca.uqac.lif.codefinder.find.FoundToken;
import ca.uqac.lif.codefinder.find.TokenFinder;

/**
 * An abstract base class for token finders that analyze the AST of a
 * Java source code file.
 */
public abstract class VisitorTokenFinder extends PushPopVisitorAdapter implements TokenFinder
{
	/** The name of the file to analyze */
	protected String m_filename;
	
	/** The name of this finder */
	protected final String m_name;
	
	/** A Java parser instance */
	protected TokenFinderContext m_context;
	
	/** The set of found tokens */
	protected final Set<FoundToken> m_found;
	
	/**
	 * The set of errors found during parsing
	 */
	protected final Set<Throwable> m_errors;
	
	/**
	 * Creates a new token finder.
	 * @param name The name of this finder
	 * @param filename The name of the file to analyze
	 */
	public VisitorTokenFinder(String name)
	{
		this(name, null);
	}
	
	/**
	 * Creates a new token finder.
	 * @param name The name of this finder
	 * @param filename The name of the file to analyze
	 * @param context A thread context
	 */
	protected VisitorTokenFinder(String name, TokenFinderContext context)
	{
		super();
		m_name = name;
		m_context = context;
		m_found = new TreeSet<FoundToken>();
		m_errors = new HashSet<Throwable>();
	}
	
	@Override
	public void addToken(int start, int end, String snippet)
	{
		m_found.add(new FoundToken(m_name, m_filename, start, end, snippet));
	}
	
	/**
	 * Gets the set of found tokens.
	 * @return The set of found tokens
	 */
	@Override
	public Set<FoundToken> getFoundTokens()
	{
		return m_found;
	}
	
	@Override
	public int getFoundCount()
	{
		return m_found.size();
	}
	
	@Override
	public Set<Throwable> getErrors()
	{
		return m_errors;
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

}	
