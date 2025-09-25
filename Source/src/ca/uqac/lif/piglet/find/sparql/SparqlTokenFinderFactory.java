package ca.uqac.lif.piglet.find.sparql;

import static ca.uqac.lif.piglet.util.Paths.getFilename;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
	public String getId()
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(m_query.getBytes());
			byte[] digest = md.digest();
			return encodeHexString(digest);
		}
		catch (NoSuchAlgorithmException e)
		{
			// This should never happen
			return "";
		}
	}

	/**
	 * Encodes a byte array as a hexadecimal string.
	 * @param byteArray The byte array
	 * @return The hexadecimal string
	 */
	protected static String encodeHexString(byte[] byteArray)
	{
		StringBuffer hexStringBuffer = new StringBuffer();
		for (int i = 0; i < byteArray.length; i++)
		{
			hexStringBuffer.append(byteToHex(byteArray[i]));
		}
		return hexStringBuffer.toString();
	}

	/**
	 * Converts a byte to a hexadecimal string.
	 * @param num The byte
	 * @return The hexadecimal string
	 */
	protected static String byteToHex(byte num)
	{
		char[] hexDigits = new char[2];
		hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
		hexDigits[1] = Character.forDigit((num & 0xF), 16);
		return new String(hexDigits);
	}

	@Override
	public SparqlTokenFinder newFinder()
	{
		return new SparqlTokenFinder(m_name, m_query);
	}

	/**
	 * Reads a SPARQL query from a file and instantiates a SPARQL token finder
	 * factory.
	 * 
	 * @param hd
	 *          The file system
	 * @param filename
	 *          The file name
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