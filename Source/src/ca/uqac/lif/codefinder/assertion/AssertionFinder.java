package ca.uqac.lif.codefinder.assertion;

import java.util.Set;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public abstract class AssertionFinder extends VoidVisitorAdapter<Set<FoundToken>>
{
	protected final String m_filename;
	
	public AssertionFinder(String filename)
	{
		super();
		m_filename = filename;
	}
	
	protected static boolean isAssertion(MethodCallExpr m)
	{
		String name = m.getName().asString();
		return name.compareTo("assert")
				* name.compareTo("assertEquals")
				* name.compareTo("assertTrue")
				* name.compareTo("assertFalse") == 0;
	}
	
	protected static boolean isAssertionEquals(MethodCallExpr m)
	{
		String name = m.getName().asString();
		return name.compareTo("assertEquals") == 0;
	}
	
	protected static boolean isAssertionNotEquals(MethodCallExpr m)
	{
		String name = m.getName().asString();
		return name.compareTo("assert")
				* name.compareTo("assertTrue")
				* name.compareTo("assertFalse") == 0;
	}
	
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
