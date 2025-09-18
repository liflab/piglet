package ca.uqac.lif.codefinder.find.visitor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.*;

import ca.uqac.lif.codefinder.find.TokenFinderContext;

public abstract class TypeAwareAssertionFinder extends VisitorAssertionFinder
{
	public TypeAwareAssertionFinder(String name) 
	{
		super(name);
	}

	protected TypeAwareAssertionFinder(String name, TokenFinderContext context) 
	{
		super(name, context);
	}

	protected abstract boolean visitTypedNode(Node n);

	@Override
	public void visit(AssignExpr n)
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(BinaryExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(BooleanLiteralExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(CastExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(CharLiteralExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(ClassExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(ConditionalExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(DoubleLiteralExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(EnclosedExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(FieldAccessExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(InstanceOfExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(IntegerLiteralExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(LambdaExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(LongLiteralExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(MethodCallExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(MethodReferenceExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(NameExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(NullLiteralExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(ObjectCreationExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(StringLiteralExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(SuperExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(ThisExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(TypeExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(UnaryExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(SwitchExpr n) 
	{
		visitTypedNode(n);
	}

	@Override
	public void visit(TextBlockLiteralExpr n) 
	{
		visitTypedNode(n);
	}

}
