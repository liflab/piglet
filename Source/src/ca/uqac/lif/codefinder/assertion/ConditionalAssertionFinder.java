package ca.uqac.lif.codefinder.assertion;

import java.util.Set;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.IfStmt;


/**
 * Finds assertions nested inside an if block.
 */
public class ConditionalAssertionFinder extends AssertionFinder
{	
	public ConditionalAssertionFinder(String filename)
	{
		super("Conditional assertions", filename);
	}

	@Override
	public AssertionFinder newFinder(String filename)
	{
		return new ConditionalAssertionFinder(filename);
	}

	@Override
	public void visit(IfStmt n, Set<FoundToken> set)
	{
		super.visit(n, set);
		findAssertions(n, n, set);

	}

	protected void findAssertions(Node source, Node n, Set<FoundToken> set)
	{
		if (n instanceof MethodCallExpr && isAssertion((MethodCallExpr) n))
		{
			int start = source.getBegin().get().line;
			int end = n.getBegin().get().line;
			set.add(new ConditionalAssertionToken(m_filename, start, end, AssertionFinder.trimLines(source.toString(), end - start + 1)));
		}
		for (Node child : n.getChildNodes())
		{
			findAssertions(source, child, set);
		}
	}

	public class ConditionalAssertionToken extends FoundToken
	{
		public ConditionalAssertionToken(String filename, int start_line, int end_line, String snippet)
		{
			super(filename, start_line, end_line, snippet);
		}

		@Override
		public String getAssertionName()
		{
			return ConditionalAssertionFinder.this.getName();
		}

	}

}
