package ca.uqac.lif.codefinder.assertion;

import java.util.List;
import java.util.Set;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;

/**
 * Finds assertions containing Boolean connectives.
 */
public class CompoundAssertionFinder extends AssertionFinder
{	
	public CompoundAssertionFinder(String filename)
	{
		super(filename);
	}

	@Override
	public void visit(MethodCallExpr n, Set<FoundToken> set)
	{
		super.visit(n, set);
		if (isAssertionNotEquals(n) && containsCompound(n))
		{
			set.add(new CompoundAssertionToken(m_filename, n.getBegin().get().line, n.toString()));
		}
	}

	protected static boolean containsCompound(Node n)
	{
		List<Node> children = n.getChildNodes();
		Node to_examine = children.get(1);
		if (children.size() == 3)
		{
			to_examine = children.get(2);
		}
		if (to_examine instanceof BinaryExpr)
		{
			BinaryExpr be = (BinaryExpr) to_examine;
			if (isComplex(be))
			{
				return true;
			}
		}
		return false;
	}

	protected static boolean isComplex(BinaryExpr be)
	{
		com.github.javaparser.ast.expr.BinaryExpr.Operator op = be.getOperator();
		switch (op)
		{
		case AND:
		case BINARY_AND:
		case BINARY_OR:
		case DIVIDE:
		case LEFT_SHIFT:
		case MINUS:
		case MULTIPLY:
		case OR:
		case PLUS:
		case REMAINDER:
		case SIGNED_RIGHT_SHIFT:
		case UNSIGNED_RIGHT_SHIFT:
		case XOR:
			return true;
		case EQUALS:
		case GREATER:
		case GREATER_EQUALS:
		case LESS:
		case LESS_EQUALS:
		case NOT_EQUALS:
		default:
			return false;
		}

	}
	
	public static class CompoundAssertionToken extends FoundToken
	{
		public CompoundAssertionToken(String filename, int line, String snippet)
		{
			super(filename, line, line, snippet);
		}		
	}
}
