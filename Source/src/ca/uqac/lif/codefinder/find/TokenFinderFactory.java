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
package ca.uqac.lif.codefinder.find;

import java.util.Set;

import ca.uqac.lif.azrael.ReadException;
import ca.uqac.lif.azrael.xml.XmlReader;
import ca.uqac.lif.fs.FileSystem;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.FileUtils;
import ca.uqac.lif.xml.XmlElement;
import ca.uqac.lif.xml.XmlElement.XmlParseException;

/**
 * A factory for creating token finders.
 * @param <T> The type of token finder created by this factory
 */
public abstract class TokenFinderFactory
{
	/**
	 * The name of this finder
	 */
	protected final String m_name;

	/**
	 * Creates a new token finder factory.
	 * 
	 * @param name
	 *          The name of this finder
	 */
	public TokenFinderFactory(String name)
	{
		super();
		m_name = name;
	}

	/**
	 * Creates a new token finder.
	 * @return A new token finder
	 */
	public abstract TokenFinder newFinder();

	/**
	 * Reads a token finder from a cache file.
	 * @param fs The file system to use for reading the cache
	 * @param project The name of the project being analyzed
	 * @return The set of found tokens
	 * @throws TokenFinderFactoryException
	 */
	@SuppressWarnings("unchecked")
	public Set<FoundToken> readCache(FileSystem fs, String project) throws TokenFinderFactoryException
	{
		try
		{
			String content = FileUtils.readStringFrom(fs, getCacheFileName(project));
			try
			{
				XmlElement x = XmlElement.parse(content);
				XmlReader r = new XmlReader();
				Set<FoundToken> f = (Set<FoundToken>) r.read(x);
				return f;
			}
			catch (XmlParseException | ReadException e)
			{
				throw new TokenFinderFactoryException(e);
			}
		} catch (FileSystemException e)
		{
			throw new TokenFinderFactoryException(e);
		}
	}

	/**
	 * Gets the	name of the cache file this finder factory and a given project.
	 * @param project The name of the project being analyzed
	 * @return The name of the cache file
	 */
	protected String getCacheFileName(String project)
	{
		return project + "/" + m_name + ".xml";
	}

	/**
	 * Indicates whether the results of this finder are cached.
	 * @param fs The file system to use for caching
	 * @param project The name of the project being analyzed
	 * @return true if the results are cached, false otherwise
	 */
	public boolean isCached(FileSystem fs, String project)
	{
		try
		{
			fs.pushd(project);
			boolean b = fs.isFile(m_name + ".xml");
			fs.popd();
			return b;
		}
		catch (FileSystemException e)
		{
			return false;
		}
	}

	/**
	 * An exception thrown when a token finder factory cannot be instantiated.
	 */
	public static class TokenFinderFactoryException extends Throwable
	{
		/**
		 * Dummy UID
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new exception with a message.
		 * @param message The message
		 */
		public TokenFinderFactoryException(String message)
		{
			super(message);
		}

		/**
		 * Creates a new exception with a cause.
		 * @param cause The cause
		 */
		public TokenFinderFactoryException(Throwable cause)
		{
			super(cause);
		}
	}
}
