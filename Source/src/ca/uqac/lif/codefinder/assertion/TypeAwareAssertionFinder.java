package ca.uqac.lif.codefinder.assertion;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.*;

import ca.uqac.lif.codefinder.thread.ThreadContext;

public abstract class TypeAwareAssertionFinder extends AssertionFinder
{
	public TypeAwareAssertionFinder(String name, String filename)
	{
		super(name, filename);
	}

	protected TypeAwareAssertionFinder(String name, String filename, ThreadContext context)
	{
		super(name, filename, context);
	}

	protected abstract boolean visitTypedNode(Node n);

	@Override
	public boolean visit(AssignExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(BinaryExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(BooleanLiteralExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(CastExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(CharLiteralExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(ClassExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(ConditionalExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(DoubleLiteralExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(EnclosedExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(FieldAccessExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(InstanceOfExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(IntegerLiteralExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(LambdaExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(LongLiteralExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(MethodCallExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(MethodReferenceExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(NameExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(NullLiteralExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(ObjectCreationExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(StringLiteralExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(SuperExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(ThisExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(TypeExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(UnaryExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(SwitchExpr n)
	{
		return visitTypedNode(n);
	}

	@Override
	public boolean visit(TextBlockLiteralExpr n)
	{
		return visitTypedNode(n);
	}

}
