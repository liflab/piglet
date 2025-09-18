package ca.uqac.lif.codefinder.find.visitor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.modules.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;

public class PushPopVisitableNode implements PushPopVisitable
{
	protected final Node m_cu;

	public PushPopVisitableNode(Node cu)
	{
		super();
		m_cu = cu;
	}

	/**
	 * Get the node contained in this visitable.
	 * @The node
	 */
	public Node getNode()
	{
		return m_cu;
	}

	@Override
	public void accept(PushPopVisitor v)
	{
		go(m_cu, v);
	}

	/**
	 * Recursively visit a node and its children with a visitor.
	 * @param n The node to visit
	 * @param v The visitor
	 */
	protected void go(Node n, PushPopVisitor v)
	{
		visitWithType(n, v);
		if (v.shouldStop())
		{
			v.reset();
			return;
		}
		for (Node child : n.getChildNodes())
		{
			go(child, v);
		}
		leaveWithType(n, v);
	}

	/**
	 * Visit a node with the appropriate typed visit method.
	 * @param n The node to visit
	 * @param v The visitor
	 * @The value returned by the visitor
	 */
	protected void visitWithType(Node n, PushPopVisitor v)
	{
		if (n instanceof AnnotationDeclaration)
			v.visit((AnnotationDeclaration) n);
		else if (n instanceof AnnotationMemberDeclaration)
			v.visit((AnnotationMemberDeclaration) n);
		else if (n instanceof ArrayAccessExpr)
			v.visit((ArrayAccessExpr) n);
		else if (n instanceof ArrayCreationExpr)
			v.visit((ArrayCreationExpr) n);
		else if (n instanceof ArrayCreationLevel)
			v.visit((ArrayCreationLevel) n);
		else if (n instanceof ArrayInitializerExpr)
			v.visit((ArrayInitializerExpr) n);
		else if (n instanceof ArrayType)
			v.visit((ArrayType) n);
		else if (n instanceof AssertStmt)
			v.visit((AssertStmt) n);
		else if (n instanceof AssignExpr)
			v.visit((AssignExpr) n);
		else if (n instanceof BinaryExpr)
			v.visit((BinaryExpr) n);
		else if (n instanceof BlockComment)
			v.visit((BlockComment) n);
		else if (n instanceof BlockStmt)
			v.visit((BlockStmt) n);
		else if (n instanceof BooleanLiteralExpr)
			v.visit((BooleanLiteralExpr) n);
		else if (n instanceof BreakStmt)
			v.visit((BreakStmt) n);
		else if (n instanceof CastExpr)
			v.visit((CastExpr) n);
		else if (n instanceof CatchClause)
			v.visit((CatchClause) n);
		else if (n instanceof CharLiteralExpr)
			v.visit((CharLiteralExpr) n);
		else if (n instanceof ClassExpr)
			v.visit((ClassExpr) n);
		else if (n instanceof ClassOrInterfaceDeclaration)
			v.visit((ClassOrInterfaceDeclaration) n);
		else if (n instanceof ClassOrInterfaceType)
			v.visit((ClassOrInterfaceType) n);
		else if (n instanceof CompilationUnit)
			v.visit((CompilationUnit) n);
		else if (n instanceof ConditionalExpr)
			v.visit((ConditionalExpr) n);
		else if (n instanceof ConstructorDeclaration)
			v.visit((ConstructorDeclaration) n);
		else if (n instanceof ContinueStmt)
			v.visit((ContinueStmt) n);
		else if (n instanceof DoStmt)
			v.visit((DoStmt) n);
		else if (n instanceof DoubleLiteralExpr)
			v.visit((DoubleLiteralExpr) n);
		else if (n instanceof EmptyStmt)
			v.visit((EmptyStmt) n);
		else if (n instanceof EnclosedExpr)
			v.visit((EnclosedExpr) n);
		else if (n instanceof EnumConstantDeclaration)
			v.visit((EnumConstantDeclaration) n);
		else if (n instanceof EnumDeclaration)
			v.visit((EnumDeclaration) n);
		else if (n instanceof ExplicitConstructorInvocationStmt)
			v.visit((ExplicitConstructorInvocationStmt) n);
		else if (n instanceof ExpressionStmt)
			v.visit((ExpressionStmt) n);
		else if (n instanceof FieldAccessExpr)
			v.visit((FieldAccessExpr) n);
		else if (n instanceof FieldDeclaration)
			v.visit((FieldDeclaration) n);
		else if (n instanceof ForStmt)
			v.visit((ForStmt) n);
		else if (n instanceof ForEachStmt)
			v.visit((ForEachStmt) n);
		else if (n instanceof IfStmt)
			v.visit((IfStmt) n);
		else if (n instanceof ImportDeclaration)
			v.visit((ImportDeclaration) n);
		else if (n instanceof InitializerDeclaration)
			v.visit((InitializerDeclaration) n);
		else if (n instanceof InstanceOfExpr)
			v.visit((InstanceOfExpr) n);
		else if (n instanceof IntegerLiteralExpr)
			v.visit((IntegerLiteralExpr) n);
		else if (n instanceof IntersectionType)
			v.visit((IntersectionType) n);
		else if (n instanceof JavadocComment)
			v.visit((JavadocComment) n);
		else if (n instanceof LabeledStmt)
			v.visit((LabeledStmt) n);
		else if (n instanceof LambdaExpr)
			v.visit((LambdaExpr) n);
		else if (n instanceof LineComment)
			v.visit((LineComment) n);
		else if (n instanceof LocalClassDeclarationStmt)
			v.visit((LocalClassDeclarationStmt) n);
		else if (n instanceof LocalRecordDeclarationStmt)
			v.visit((LocalRecordDeclarationStmt) n);
		else if (n instanceof LongLiteralExpr)
			v.visit((LongLiteralExpr) n);
		else if (n instanceof MarkerAnnotationExpr)
			v.visit((MarkerAnnotationExpr) n);
		else if (n instanceof MemberValuePair)
			v.visit((MemberValuePair) n);
		else if (n instanceof MethodCallExpr)
			v.visit((MethodCallExpr) n);
		else if (n instanceof MethodDeclaration)
			v.visit((MethodDeclaration) n);
		else if (n instanceof MethodReferenceExpr)
			v.visit((MethodReferenceExpr) n);
		else if (n instanceof NameExpr)
			v.visit((NameExpr) n);
		else if (n instanceof Name)
			v.visit((Name) n);
		else if (n instanceof NormalAnnotationExpr)
			v.visit((NormalAnnotationExpr) n);
		else if (n instanceof NullLiteralExpr)
			v.visit((NullLiteralExpr) n);
		else if (n instanceof ObjectCreationExpr)
			v.visit((ObjectCreationExpr) n);
		else if (n instanceof PackageDeclaration)
			v.visit((PackageDeclaration) n);
		else if (n instanceof Parameter)
			v.visit((Parameter) n);
		else if (n instanceof PrimitiveType)
			v.visit((PrimitiveType) n);
		else if (n instanceof RecordDeclaration)
			v.visit((RecordDeclaration) n);
		else if (n instanceof CompactConstructorDeclaration)
			v.visit((CompactConstructorDeclaration) n);
		else if (n instanceof ReturnStmt)
			v.visit((ReturnStmt) n);
		else if (n instanceof SimpleName)
			v.visit((SimpleName) n);
		else if (n instanceof SingleMemberAnnotationExpr)
			v.visit((SingleMemberAnnotationExpr) n);
		else if (n instanceof StringLiteralExpr)
			v.visit((StringLiteralExpr) n);
		else if (n instanceof SuperExpr)
			v.visit((SuperExpr) n);
		else if (n instanceof SwitchEntry)
			v.visit((SwitchEntry) n);
		else if (n instanceof SwitchStmt)
			v.visit((SwitchStmt) n);
		else if (n instanceof SynchronizedStmt)
			v.visit((SynchronizedStmt) n);
		else if (n instanceof ThisExpr)
			v.visit((ThisExpr) n);
		else if (n instanceof ThrowStmt)
			v.visit((ThrowStmt) n);
		else if (n instanceof TryStmt)
			v.visit((TryStmt) n);
		else if (n instanceof TypeExpr)
			v.visit((TypeExpr) n);
		else if (n instanceof TypeParameter)
			v.visit((TypeParameter) n);
		else if (n instanceof UnaryExpr)
			v.visit((UnaryExpr) n);
		else if (n instanceof UnionType)
			v.visit((UnionType) n);
		else if (n instanceof UnknownType)
			v.visit((UnknownType) n);
		else if (n instanceof VariableDeclarationExpr)
			v.visit((VariableDeclarationExpr) n);
		else if (n instanceof VariableDeclarator)
			v.visit((VariableDeclarator) n);
		else if (n instanceof VoidType)
			v.visit((VoidType) n);
		else if (n instanceof WhileStmt)
			v.visit((WhileStmt) n);
		else if (n instanceof WildcardType)
			v.visit((WildcardType) n);
		else if (n instanceof ModuleDeclaration)
			v.visit((ModuleDeclaration) n);
		else if (n instanceof ModuleRequiresDirective)
			v.visit((ModuleRequiresDirective) n);
		else if (n instanceof ModuleExportsDirective)
			v.visit((ModuleExportsDirective) n);
		else if (n instanceof ModuleProvidesDirective)
			v.visit((ModuleProvidesDirective) n);
		else if (n instanceof ModuleUsesDirective)
			v.visit((ModuleUsesDirective) n);
		else if (n instanceof ModuleOpensDirective)
			v.visit((ModuleOpensDirective) n);
		else if (n instanceof UnparsableStmt)
			v.visit((UnparsableStmt) n);
		else if (n instanceof ReceiverParameter)
			v.visit((ReceiverParameter) n);
		else if (n instanceof VarType)
			v.visit((VarType) n);
		else if (n instanceof Modifier)
			v.visit((Modifier) n);
		else if (n instanceof SwitchExpr)
			v.visit((SwitchExpr) n);
		else if (n instanceof TextBlockLiteralExpr)
			v.visit((TextBlockLiteralExpr) n);
		else if (n instanceof YieldStmt)
			v.visit((YieldStmt) n);
		else if (n instanceof TypePatternExpr)
			v.visit((TypePatternExpr) n);
		else if (n instanceof RecordPatternExpr)
			v.visit((RecordPatternExpr) n);
	}

	/**
	 * Leave a node with the appropriate typed leave method.
	 * @param n The node to leave
	 * @param v The visitor
	 */
	protected void leaveWithType(Node n, PushPopVisitor v)
	{
		if (n instanceof AnnotationDeclaration)
			v.leave((AnnotationDeclaration) n);
		else if (n instanceof AnnotationMemberDeclaration)
			v.leave((AnnotationMemberDeclaration) n);
		else if (n instanceof ArrayAccessExpr)
			v.leave((ArrayAccessExpr) n);
		else if (n instanceof ArrayCreationExpr)
			v.leave((ArrayCreationExpr) n);
		else if (n instanceof ArrayCreationLevel)
			v.leave((ArrayCreationLevel) n);
		else if (n instanceof ArrayInitializerExpr)
			v.leave((ArrayInitializerExpr) n);
		else if (n instanceof ArrayType)
			v.leave((ArrayType) n);
		else if (n instanceof AssertStmt)
			v.leave((AssertStmt) n);
		else if (n instanceof AssignExpr)
			v.leave((AssignExpr) n);
		else if (n instanceof BinaryExpr)
			v.leave((BinaryExpr) n);
		else if (n instanceof BlockComment)
			v.leave((BlockComment) n);
		else if (n instanceof BlockStmt)
			v.leave((BlockStmt) n);
		else if (n instanceof BooleanLiteralExpr)
			v.leave((BooleanLiteralExpr) n);
		else if (n instanceof BreakStmt)
			v.leave((BreakStmt) n);
		else if (n instanceof CastExpr)
			v.leave((CastExpr) n);
		else if (n instanceof CatchClause)
			v.leave((CatchClause) n);
		else if (n instanceof CharLiteralExpr)
			v.leave((CharLiteralExpr) n);
		else if (n instanceof ClassExpr)
			v.leave((ClassExpr) n);
		else if (n instanceof ClassOrInterfaceDeclaration)
			v.leave((ClassOrInterfaceDeclaration) n);
		else if (n instanceof ClassOrInterfaceType)
			v.leave((ClassOrInterfaceType) n);
		else if (n instanceof CompilationUnit)
			v.leave((CompilationUnit) n);
		else if (n instanceof ConditionalExpr)
			v.leave((ConditionalExpr) n);
		else if (n instanceof ConstructorDeclaration)
			v.leave((ConstructorDeclaration) n);
		else if (n instanceof ContinueStmt)
			v.leave((ContinueStmt) n);
		else if (n instanceof DoStmt)
			v.leave((DoStmt) n);
		else if (n instanceof DoubleLiteralExpr)
			v.leave((DoubleLiteralExpr) n);
		else if (n instanceof EmptyStmt)
			v.leave((EmptyStmt) n);
		else if (n instanceof EnclosedExpr)
			v.leave((EnclosedExpr) n);
		else if (n instanceof EnumConstantDeclaration)
			v.leave((EnumConstantDeclaration) n);
		else if (n instanceof EnumDeclaration)
			v.leave((EnumDeclaration) n);
		else if (n instanceof ExplicitConstructorInvocationStmt)
			v.leave((ExplicitConstructorInvocationStmt) n);
		else if (n instanceof ExpressionStmt)
			v.leave((ExpressionStmt) n);
		else if (n instanceof FieldAccessExpr)
			v.leave((FieldAccessExpr) n);
		else if (n instanceof FieldDeclaration)
			v.leave((FieldDeclaration) n);
		else if (n instanceof ForStmt)
			v.leave((ForStmt) n);
		else if (n instanceof ForEachStmt)
			v.leave((ForEachStmt) n);
		else if (n instanceof IfStmt)
			v.leave((IfStmt) n);
		else if (n instanceof ImportDeclaration)
			v.leave((ImportDeclaration) n);
		else if (n instanceof InitializerDeclaration)
			v.leave((InitializerDeclaration) n);
		else if (n instanceof InstanceOfExpr)
			v.leave((InstanceOfExpr) n);
		else if (n instanceof IntegerLiteralExpr)
			v.leave((IntegerLiteralExpr) n);
		else if (n instanceof IntersectionType)
			v.leave((IntersectionType) n);
		else if (n instanceof JavadocComment)
			v.leave((JavadocComment) n);
		else if (n instanceof LabeledStmt)
			v.leave((LabeledStmt) n);
		else if (n instanceof LambdaExpr)
			v.leave((LambdaExpr) n);
		else if (n instanceof LineComment)
			v.leave((LineComment) n);
		else if (n instanceof LocalClassDeclarationStmt)
			v.leave((LocalClassDeclarationStmt) n);
		else if (n instanceof LocalRecordDeclarationStmt)
			v.leave((LocalRecordDeclarationStmt) n);
		else if (n instanceof LongLiteralExpr)
			v.leave((LongLiteralExpr) n);
		else if (n instanceof MarkerAnnotationExpr)
			v.leave((MarkerAnnotationExpr) n);
		else if (n instanceof MemberValuePair)
			v.leave((MemberValuePair) n);
		else if (n instanceof MethodCallExpr)
			v.leave((MethodCallExpr) n);
		else if (n instanceof MethodDeclaration)
			v.leave((MethodDeclaration) n);
		else if (n instanceof MethodReferenceExpr)
			v.leave((MethodReferenceExpr) n);
		else if (n instanceof NameExpr)
			v.leave((NameExpr) n);
		else if (n instanceof Name)
			v.leave((Name) n);
		else if (n instanceof NormalAnnotationExpr)
			v.leave((NormalAnnotationExpr) n);
		else if (n instanceof NullLiteralExpr)
			v.leave((NullLiteralExpr) n);
		else if (n instanceof ObjectCreationExpr)
			v.leave((ObjectCreationExpr) n);
		else if (n instanceof PackageDeclaration)
			v.leave((PackageDeclaration) n);
		else if (n instanceof Parameter)
			v.leave((Parameter) n);
		else if (n instanceof PrimitiveType)
			v.leave((PrimitiveType) n);
		else if (n instanceof RecordDeclaration)
			v.leave((RecordDeclaration) n);
		else if (n instanceof CompactConstructorDeclaration)
			v.leave((CompactConstructorDeclaration) n);
		else if (n instanceof ReturnStmt)
			v.leave((ReturnStmt) n);
		else if (n instanceof SimpleName)
			v.leave((SimpleName) n);
		else if (n instanceof SingleMemberAnnotationExpr)
			v.leave((SingleMemberAnnotationExpr) n);
		else if (n instanceof StringLiteralExpr)
			v.leave((StringLiteralExpr) n);
		else if (n instanceof SuperExpr)
			v.leave((SuperExpr) n);
		else if (n instanceof SwitchEntry)
			v.leave((SwitchEntry) n);
		else if (n instanceof SwitchStmt)
			v.leave((SwitchStmt) n);
		else if (n instanceof SynchronizedStmt)
			v.leave((SynchronizedStmt) n);
		else if (n instanceof ThisExpr)
			v.leave((ThisExpr) n);
		else if (n instanceof ThrowStmt)
			v.leave((ThrowStmt) n);
		else if (n instanceof TryStmt)
			v.leave((TryStmt) n);
		else if (n instanceof TypeExpr)
			v.leave((TypeExpr) n);
		else if (n instanceof TypeParameter)
			v.leave((TypeParameter) n);
		else if (n instanceof UnaryExpr)
			v.leave((UnaryExpr) n);
		else if (n instanceof UnionType)
			v.leave((UnionType) n);
		else if (n instanceof UnknownType)
			v.leave((UnknownType) n);
		else if (n instanceof VariableDeclarationExpr)
			v.leave((VariableDeclarationExpr) n);
		else if (n instanceof VariableDeclarator)
			v.leave((VariableDeclarator) n);
		else if (n instanceof VoidType)
			v.leave((VoidType) n);
		else if (n instanceof WhileStmt)
			v.leave((WhileStmt) n);
		else if (n instanceof WildcardType)
			v.leave((WildcardType) n);
		else if (n instanceof ModuleDeclaration)
			v.leave((ModuleDeclaration) n);
		else if (n instanceof ModuleRequiresDirective)
			v.leave((ModuleRequiresDirective) n);
		else if (n instanceof ModuleExportsDirective)
			v.leave((ModuleExportsDirective) n);
		else if (n instanceof ModuleProvidesDirective)
			v.leave((ModuleProvidesDirective) n);
		else if (n instanceof ModuleUsesDirective)
			v.leave((ModuleUsesDirective) n);
		else if (n instanceof ModuleOpensDirective)
			v.leave((ModuleOpensDirective) n);
		else if (n instanceof UnparsableStmt)
			v.leave((UnparsableStmt) n);
		else if (n instanceof ReceiverParameter)
			v.leave((ReceiverParameter) n);
		else if (n instanceof VarType)
			v.leave((VarType) n);
		else if (n instanceof Modifier)
			v.leave((Modifier) n);
		else if (n instanceof SwitchExpr)
			v.leave((SwitchExpr) n);
		else if (n instanceof TextBlockLiteralExpr)
			v.leave((TextBlockLiteralExpr) n);
		else if (n instanceof YieldStmt)
			v.leave((YieldStmt) n);
		else if (n instanceof TypePatternExpr)
			v.leave((TypePatternExpr) n);
		else if (n instanceof RecordPatternExpr)
			v.leave((RecordPatternExpr) n);
	}
}
