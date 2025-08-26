package ca.uqac.lif.codefinder.assertion;

import java.util.Set;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public abstract class AssertionFinder extends VoidVisitorAdapter<Set<FoundToken>>
{
	/** The name of the file to analyze */
	protected final String m_filename;
	
	/** The name of this finder */
	protected final String m_name;
	
	/**
	 * Creates a new assertion finder.
	 * @param name The name of this finder
	 * @param filename The name of the file to analyze
	 */
	public AssertionFinder(String name, String filename)
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
	public abstract AssertionFinder newFinder(String filename);
	
	/**
	 * Determines if a method call expression is an assertion.
	 * @param m The method call expression to examine
	 * @return <tt>true</tt> if the method call is an assertion, <tt>false</tt>
	 * otherwise
	 */
	protected static boolean isAssertion(MethodCallExpr m)
	{
		String name = m.getName().asString();
		return name.compareTo("assert")
				* name.compareTo("assertEquals")
				* name.compareTo("assertTrue")
				* name.compareTo("assertFalse") == 0;
	}
	
	/**
	 * Determines if a method call expression is an assertEquals call.
	 * @param m The method call expression to examine
	 * @return <tt>true</tt> if the method call is an assertEquals call, <tt>false</tt>
	 * otherwise
	 */
	protected static boolean isAssertionEquals(MethodCallExpr m)
	{
		String name = m.getName().asString();
		return name.compareTo("assertEquals") == 0;
	}
	
	/**
	 * Determines if a method call expression is an assertion, but that is not
	 * assertEquals.
	 * @param m The method call expression to examine
	 * @return <tt>true</tt> if the method call is an assertion other than
	 * assertEquals, <tt>false</tt> otherwise
	 */
	protected static boolean isAssertionNotEquals(MethodCallExpr m)
	{
		String name = m.getName().asString();
		return name.compareTo("assert")
				* name.compareTo("assertTrue")
				* name.compareTo("assertFalse") == 0;
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
