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

import java.util.Set;

import com.github.javaparser.ast.Node;

import ca.uqac.lif.azrael.ObjectPrinter;
import ca.uqac.lif.azrael.ObjectReader;
import ca.uqac.lif.azrael.PrintException;
import ca.uqac.lif.azrael.Printable;
import ca.uqac.lif.azrael.ReadException;
import ca.uqac.lif.azrael.Readable;

public interface TokenFinder extends Printable, Readable
{
	/**
	 * Gets the name of this finder.
	 * @return The name of this finder
	 */
	public String getName();

	/**
	 * Gets the number of tokens found by this finder.
	 * @return The number of tokens found
	 */
	public int getFoundCount();

	/**
	 * Gets the errors encountered during the analysis.
	 * @return A collection of errors encountered during the analysis
	 */
	public Set<Throwable> getErrors();

	/**
	 * Gets all tokens found by this finder.
	 * @return A collection of found tokens. If the method returns
	 * <tt>null</tt>, it means that the finder has only counted the tokens
	 * without storing them.
	 */
	public Set<FoundToken> getFoundTokens();

	/**
	 * Sets the thread context in which this finder operates.
	 * @param context The thread context
	 */
	public void setContext(TokenFinderContext context);

	/**
	 * Sets the name of the file to analyze. This is used to
	 * put inside the found tokens.
	 * @param filename The name of the file to analyze
	 */
	public void setFilename(String filename);

	/**
	 * Adds a found token based on a given AST node.
	 * 
	 * @param n
	 *          The AST node that represents the found token
	 */
	public default void addToken(Node n)
	{
		addToken(n.getBegin().get().line, n.getEnd().get().line, n.toString());
	}

	/**
	 * Adds a found token based on character offsets.
	 * 
	 * @param start
	 *          The start offset of the found token
	 * @param end
	 *          The end offset of the found token
	 * @param snippet
	 *          A snippet of code corresponding to the found token
	 */
	public void addToken(int start, int end, String snippet);
	
	@Override
	public TokenFinder read(ObjectReader<?> r, Object o) throws ReadException;
	
	@Override
	public Object print(ObjectPrinter<?> p) throws PrintException;
	
	/**
	 * An exception thrown when a token finder cannot process a file.
	 */
	public static class TokenFinderException extends Throwable
	{
		/**
		 * Dummy UID
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new exception with a message.
		 * @param message The message
		 */
		public TokenFinderException(String message)
		{
			super(message);
		}

		/**
		 * Creates a new exception with a cause.
		 * @param cause The cause
		 */
		public TokenFinderException(Throwable cause)
		{
			super(cause);
		}
		
		/**
		 * Creates a new exception with a message and a cause.
		 * @param message The message
		 * @param cause The cause
		 */
		public TokenFinderException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}
}
