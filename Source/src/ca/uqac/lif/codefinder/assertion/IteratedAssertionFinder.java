package ca.uqac.lif.codefinder.assertion;

import java.util.Set;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

/**
 * Finds assertions nested inside a for, while or do block.
 */
public class IteratedAssertionFinder extends AssertionFinder
{	
	public IteratedAssertionFinder(String filename)
	{
		super("Iterated assertions", filename);
	}
	
	@Override
	public AssertionFinder newFinder(String filename)
	{
		return new IteratedAssertionFinder(filename);
	}
	
	@Override
	public void visit(ForStmt n, Set<FoundToken> set)
	{
		super.visit(n, set);
		findAssertions(n, n, set);
	}
	
	@Override
	public void visit(DoStmt n, Set<FoundToken> set)
	{
		super.visit(n, set);
		findAssertions(n, n, set);
	}
	
	@Override
	public void visit(WhileStmt n, Set<FoundToken> set)
	{
		super.visit(n, set);
		findAssertions(n, n, set);
	}
	
	public class IteratedAssertionToken extends FoundToken
	{
		public IteratedAssertionToken(String filename, int start_line, int end_line, String snippet)
		{
			super(filename, start_line, end_line, snippet);
		}	
		
		@Override
		public String getAssertionName()
		{
			return IteratedAssertionFinder.this.getName();
		}
	}
	
	protected void findAssertions(Node source, Node n, Set<FoundToken> set)
	{
		if (n instanceof MethodCallExpr && isAssertion((MethodCallExpr) n))
		{
			int start = source.getBegin().get().line;
			int end = n.getBegin().get().line;
			set.add(new IteratedAssertionToken(m_filename, start, end, AssertionFinder.trimLines(source.toString(), end - start + 1)));
		}
		for (Node child : n.getChildNodes())
		{
			findAssertions(source, child, set);
		}
	}
}
