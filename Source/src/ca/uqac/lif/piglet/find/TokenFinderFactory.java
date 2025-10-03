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

import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import ca.uqac.lif.azrael.ReadException;
import ca.uqac.lif.azrael.json.JsonReader;
import ca.uqac.lif.fs.FileSystem;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.FileUtils;
import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonParser;
import ca.uqac.lif.json.JsonParser.JsonParseException;
import ca.uqac.lif.piglet.report.Report;

/**
 * A factory for creating token finders.
 * 
 * @param <T>
 *          The type of token finder created by this factory
 */
public abstract class TokenFinderFactory
{
	/**
	 * The name of this finder
	 */
	protected final String m_name;

	/**
	 * The number of expected results (used for progress reporting)
	 */
	private final LongAdder expected = new LongAdder();

	/**
	 * The number of finished results (used for progress reporting)
	 */
	private final LongAdder finished = new LongAdder();

	/** Call once per file where this finder is expected to run. */
	public final void registerExpected()
	{
		expected.increment();
	}

	/** Call only when the run completed successfully. */
	public final void registerFinished()
	{
		finished.increment();
	}

	/**
	 * Gets the number of expected runs for this finder.
	 * @return The number of expected runs
	 */
	public final long expectedCount()
	{
		return expected.sum();
	}

	/**
	 * Gets the number of finished runs for this finder.
	 * @return The number of finished runs
	 */
	public final long finishedCount()
	{
		return finished.sum();
	}

	/**
	 * Gets the number of runs not yet finished for this finder.
	 * @return The number of runs not yet finished
	 */
	public final long notFinishedCount()
	{
		return Math.max(0, expected.sum() - finished.sum());
	}

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
	 * Gets the name of this finder.
	 * 
	 * @return The name of this finder
	 */
	public final String getName()
	{
		return m_name;
	}

	/**
	 * Gets a unique identifier for this finder. The goal of this identifier is to
	 * detect when the implementation of the finder has changed, which invalidates
	 * the cached results. It is up to the implementer of the finder to ensure that
	 * this identifier changes when the implementation changes.
	 * 
	 * @return A unique identifier for this finder
	 */
	public abstract String getId();

	/**
	 * Creates a new token finder.
	 * 
	 * @return A new token finder
	 */
	public abstract TokenFinder newFinder();

	/**
	 * Reads a token finder from a cache file.
	 * 
	 * @param fs
	 *          The file system to use for reading the cache
	 * @param project
	 *          The name of the project being analyzed
	 * @return The set of found tokens
	 * @throws TokenFinderFactoryException
	 */
	@SuppressWarnings("unchecked")
	public List<FoundToken> readCache(FileSystem fs, String project, boolean force_cache)
			throws TokenFinderFactoryException
	{
		try
		{
			String content = FileUtils.readStringFrom(fs, getCacheFileName(project));
			try
			{
				JsonParser parser = new JsonParser();
				JsonElement x = parser.parse(content);
				JsonReader r = new JsonReader();
				List<Object> l = (List<Object>) r.read(x);
				List<FoundToken> tokens = null;
				String hash = null;
				if (l.get(0) instanceof List<?> && !force_cache)
				{
					// Old format without hash, ignore cache
					if (force_cache)
					{
						tokens = (List<FoundToken>) l.get(0);
						System.err.println("Warning: cache file for finder \"" + m_name + "\" is outdated");
					}
					else
					{
						return null;
					}
				}
				else
				{
					hash = (String) l.get(0);
					tokens = (List<FoundToken>) l.get(1);
				}
				if (hash != null && !hash.equals(getId()))
				{
					System.err.println("Warning: cache file for finder \"" + m_name + "\" is outdated");
					return null;
				}
				return tokens;
			}
			catch (JsonParseException | ReadException e)
			{
				throw new TokenFinderFactoryException(e);
			}
		}
		catch (FileSystemException e)
		{
			throw new TokenFinderFactoryException(e);
		}
	}

	/**
	 * Gets the name of the cache file this finder factory and a given project.
	 * 
	 * @param project
	 *          The name of the project being analyzed
	 * @return The name of the cache file
	 */
	public String getCacheFileName(String project)
	{
		return project + "/" + escape(m_name) + ".json";
	}

	/**
	 * Indicates whether the results of this finder are cached.
	 * 
	 * @param fs
	 *          The file system to use for caching
	 * @param project
	 *          The name of the project being analyzed
	 * @return true if the results are cached, false otherwise
	 */
	public boolean isCached(FileSystem fs, String project)
	{
		try
		{
			fs.pushd(project);
			boolean b = fs.isFile(escape(m_name) + ".json");
			fs.popd();
			return b;
		}
		catch (FileSystemException e)
		{
			return false;
		}
	}

	/**
	 * Escapes a string to be used as a file name.
	 * 
	 * @param s
	 *          The string to escape
	 * @return The escaped string
	 */
	public static String escape(String s)
	{
		return s.replaceAll(Report.PATH_SEPARATOR, "_");
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
		 * 
		 * @param message
		 *          The message
		 */
		public TokenFinderFactoryException(String message)
		{
			super(message);
		}

		/**
		 * Creates a new exception with a cause.
		 * 
		 * @param cause
		 *          The cause
		 */
		public TokenFinderFactoryException(Throwable cause)
		{
			super(cause);
		}
	}
}
