package ca.uqac.lif.piglet.find.sparql;

import static ca.uqac.lif.piglet.util.Paths.getFilename;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uqac.lif.fs.FileSystem;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.piglet.find.TokenFinderFactory;

public class SparqlTokenFinderFactory extends TokenFinderFactory
{
	/**
	 * The SPARQL query to execute.
	 */
	protected final String m_query;

	/**
	 * Pattern to extract the name of an assertion from a comment 
	 */
	protected static final Pattern s_namePat = Pattern.compile("Name:([^\\*]+)");

	public SparqlTokenFinderFactory(String name, String query)
	{
		super(name);
		m_query = query;
	}

	@Override
	public SparqlTokenFinder newFinder()
	{
		return new SparqlTokenFinder(m_name, m_query);
	}

	/**
	 * Reads a SPARQL query from a file and instantiates a
	 * SPARQL token finder factory.
	 * @param hd The file system
	 * @param filename The file name
	 * @return A SPARQL token finder factory
	 * @throws TokenFinderFactoryException
	 */
	public static SparqlTokenFinderFactory readSparql(FileSystem hd, String filename)
			throws TokenFinderFactoryException
	{
		StringBuilder sparql_code = new StringBuilder();
		String name = null;
		try
		{
			Scanner scanner = new Scanner(hd.readFrom(getFilename(filename)));
			while (scanner.hasNextLine())
			{
				String line = scanner.nextLine().trim();
				if (line.isEmpty())
					continue;
				if (line.startsWith("#"))
				{
					if (name == null)
					{
						Matcher mat = s_namePat.matcher(line);
						if (mat.find())
						{
							name = mat.group(1).trim();
						}
					}
					continue;
				}
				sparql_code.append(line).append("\n");
			}
			scanner.close();
			return new SparqlTokenFinderFactory(name == null ? "Unnamed SPARQL finder" : name,
					sparql_code.toString());
		}
		catch (FileSystemException e)
		{
			throw new TokenFinderFactoryException(e);
		}
	}
}