package ca.uqac.lif.codefinder.assertion;

/**
 * Represents a token found in a source file, such as an assertion or code fragment.
 * Stores the filename, start and end line numbers, and an optional code snippet.
 */
public abstract class FoundToken implements Comparable<FoundToken>
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
	 * Constructs a FoundToken with a code snippet.
	 * @param filename The name of the file
	 * @param start_line The starting line number
	 * @param end_line The ending line number
	 * @param snippet The code snippet
	 */
	public FoundToken(String filename, int start_line, int end_line, String snippet)
	{
		super();
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
	public FoundToken(String filename, int start_line, int end_line)
	{
		this(filename, start_line, end_line, "");
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
	public abstract String getAssertionName();

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
}