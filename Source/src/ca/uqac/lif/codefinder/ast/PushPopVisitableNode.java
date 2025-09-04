package ca.uqac.lif.codefinder.ast;

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

	@Override
	public void accept(PushPopVisitor v)
	{
		go(m_cu, v);
	}

	protected void go(Node n, PushPopVisitor v)
	{
		boolean b = visitWithType(n, v);
		if (b)
		{
			for (Node child : n.getChildNodes())
			{
				go(child, v);
			}
		}
		leaveWithType(n, v);
	}

	protected boolean visitWithType(Node n, PushPopVisitor v)
	{
		if (n instanceof AnnotationDeclaration)
			return v.visit((AnnotationDeclaration) n);
		else if (n instanceof AnnotationMemberDeclaration)
			return v.visit((AnnotationMemberDeclaration) n);
		else if (n instanceof ArrayAccessExpr)
			return v.visit((ArrayAccessExpr) n);
		else if (n instanceof ArrayCreationExpr)
			return v.visit((ArrayCreationExpr) n);
		else if (n instanceof ArrayCreationLevel)
			return v.visit((ArrayCreationLevel) n);
		else if (n instanceof ArrayInitializerExpr)
			return v.visit((ArrayInitializerExpr) n);
		else if (n instanceof ArrayType)
			return v.visit((ArrayType) n);
		else if (n instanceof AssertStmt)
			return v.visit((AssertStmt) n);
		else if (n instanceof AssignExpr)
			return v.visit((AssignExpr) n);
		else if (n instanceof BinaryExpr)
			return v.visit((BinaryExpr) n);
		else if (n instanceof BlockComment)
			return v.visit((BlockComment) n);
		else if (n instanceof BlockStmt)
			return v.visit((BlockStmt) n);
		else if (n instanceof BooleanLiteralExpr)
			return v.visit((BooleanLiteralExpr) n);
		else if (n instanceof BreakStmt)
			return v.visit((BreakStmt) n);
		else if (n instanceof CastExpr)
			return v.visit((CastExpr) n);
		else if (n instanceof CatchClause)
			return v.visit((CatchClause) n);
		else if (n instanceof CharLiteralExpr)
			return v.visit((CharLiteralExpr) n);
		else if (n instanceof ClassExpr)
			return v.visit((ClassExpr) n);
		else if (n instanceof ClassOrInterfaceDeclaration)
			return v.visit((ClassOrInterfaceDeclaration) n);
		else if (n instanceof ClassOrInterfaceType)
			return v.visit((ClassOrInterfaceType) n);
		else if (n instanceof CompilationUnit)
			return v.visit((CompilationUnit) n);
		else if (n instanceof ConditionalExpr)
			return v.visit((ConditionalExpr) n);
		else if (n instanceof ConstructorDeclaration)
			return v.visit((ConstructorDeclaration) n);
		else if (n instanceof ContinueStmt)
			return v.visit((ContinueStmt) n);
		else if (n instanceof DoStmt)
			return v.visit((DoStmt) n);
		else if (n instanceof DoubleLiteralExpr)
			return v.visit((DoubleLiteralExpr) n);
		else if (n instanceof EmptyStmt)
			return v.visit((EmptyStmt) n);
		else if (n instanceof EnclosedExpr)
			return v.visit((EnclosedExpr) n);
		else if (n instanceof EnumConstantDeclaration)
			return v.visit((EnumConstantDeclaration) n);
		else if (n instanceof EnumDeclaration)
			return v.visit((EnumDeclaration) n);
		else if (n instanceof ExplicitConstructorInvocationStmt)
			return v.visit((ExplicitConstructorInvocationStmt) n);
		else if (n instanceof ExpressionStmt)
			return v.visit((ExpressionStmt) n);
		else if (n instanceof FieldAccessExpr)
			return v.visit((FieldAccessExpr) n);
		else if (n instanceof FieldDeclaration)
			return v.visit((FieldDeclaration) n);
		else if (n instanceof ForStmt)
			return v.visit((ForStmt) n);
		else if (n instanceof ForEachStmt)
			return v.visit((ForEachStmt) n);
		else if (n instanceof IfStmt)
			return v.visit((IfStmt) n);
		else if (n instanceof ImportDeclaration)
			return v.visit((ImportDeclaration) n);
		else if (n instanceof InitializerDeclaration)
			return v.visit((InitializerDeclaration) n);
		else if (n instanceof InstanceOfExpr)
			return v.visit((InstanceOfExpr) n);
		else if (n instanceof IntegerLiteralExpr)
			return v.visit((IntegerLiteralExpr) n);
		else if (n instanceof IntersectionType)
			return v.visit((IntersectionType) n);
		else if (n instanceof JavadocComment)
			return v.visit((JavadocComment) n);
		else if (n instanceof LabeledStmt)
			return v.visit((LabeledStmt) n);
		else if (n instanceof LambdaExpr)
			return v.visit((LambdaExpr) n);
		else if (n instanceof LineComment)
			return v.visit((LineComment) n);
		else if (n instanceof LocalClassDeclarationStmt)
			return v.visit((LocalClassDeclarationStmt) n);
		else if (n instanceof LocalRecordDeclarationStmt)
			return v.visit((LocalRecordDeclarationStmt) n);
		else if (n instanceof LongLiteralExpr)
			return v.visit((LongLiteralExpr) n);
		else if (n instanceof MarkerAnnotationExpr)
			return v.visit((MarkerAnnotationExpr) n);
		else if (n instanceof MemberValuePair)
			return v.visit((MemberValuePair) n);
		else if (n instanceof MethodCallExpr)
			return v.visit((MethodCallExpr) n);
		else if (n instanceof MethodDeclaration)
			return v.visit((MethodDeclaration) n);
		else if (n instanceof MethodReferenceExpr)
			return v.visit((MethodReferenceExpr) n);
		else if (n instanceof NameExpr)
			return v.visit((NameExpr) n);
		else if (n instanceof Name)
			return v.visit((Name) n);
		else if (n instanceof NormalAnnotationExpr)
			return v.visit((NormalAnnotationExpr) n);
		else if (n instanceof NullLiteralExpr)
			return v.visit((NullLiteralExpr) n);
		else if (n instanceof ObjectCreationExpr)
			return v.visit((ObjectCreationExpr) n);
		else if (n instanceof PackageDeclaration)
			return v.visit((PackageDeclaration) n);
		else if (n instanceof Parameter)
			return v.visit((Parameter) n);
		else if (n instanceof PrimitiveType)
			return v.visit((PrimitiveType) n);
		else if (n instanceof RecordDeclaration)
			return v.visit((RecordDeclaration) n);
		else if (n instanceof CompactConstructorDeclaration)
			return v.visit((CompactConstructorDeclaration) n);
		else if (n instanceof ReturnStmt)
			return v.visit((ReturnStmt) n);
		else if (n instanceof SimpleName)
			return v.visit((SimpleName) n);
		else if (n instanceof SingleMemberAnnotationExpr)
			return v.visit((SingleMemberAnnotationExpr) n);
		else if (n instanceof StringLiteralExpr)
			return v.visit((StringLiteralExpr) n);
		else if (n instanceof SuperExpr)
			return v.visit((SuperExpr) n);
		else if (n instanceof SwitchEntry)
			return v.visit((SwitchEntry) n);
		else if (n instanceof SwitchStmt)
			return v.visit((SwitchStmt) n);
		else if (n instanceof SynchronizedStmt)
			return v.visit((SynchronizedStmt) n);
		else if (n instanceof ThisExpr)
			return v.visit((ThisExpr) n);
		else if (n instanceof ThrowStmt)
			return v.visit((ThrowStmt) n);
		else if (n instanceof TryStmt)
			return v.visit((TryStmt) n);
		else if (n instanceof TypeExpr)
			return v.visit((TypeExpr) n);
		else if (n instanceof TypeParameter)
			return v.visit((TypeParameter) n);
		else if (n instanceof UnaryExpr)
			return v.visit((UnaryExpr) n);
		else if (n instanceof UnionType)
			return v.visit((UnionType) n);
		else if (n instanceof UnknownType)
			return v.visit((UnknownType) n);
		else if (n instanceof VariableDeclarationExpr)
			return v.visit((VariableDeclarationExpr) n);
		else if (n instanceof VariableDeclarator)
			return v.visit((VariableDeclarator) n);
		else if (n instanceof VoidType)
			return v.visit((VoidType) n);
		else if (n instanceof WhileStmt)
			return v.visit((WhileStmt) n);
		else if (n instanceof WildcardType)
			return v.visit((WildcardType) n);
		else if (n instanceof ModuleDeclaration)
			return v.visit((ModuleDeclaration) n);
		else if (n instanceof ModuleRequiresDirective)
			return v.visit((ModuleRequiresDirective) n);
		else if (n instanceof ModuleExportsDirective)
			return v.visit((ModuleExportsDirective) n);
		else if (n instanceof ModuleProvidesDirective)
			return v.visit((ModuleProvidesDirective) n);
		else if (n instanceof ModuleUsesDirective)
			return v.visit((ModuleUsesDirective) n);
		else if (n instanceof ModuleOpensDirective)
			return v.visit((ModuleOpensDirective) n);
		else if (n instanceof UnparsableStmt)
			return v.visit((UnparsableStmt) n);
		else if (n instanceof ReceiverParameter)
			return v.visit((ReceiverParameter) n);
		else if (n instanceof VarType)
			return v.visit((VarType) n);
		else if (n instanceof Modifier)
			return v.visit((Modifier) n);
		else if (n instanceof SwitchExpr)
			return v.visit((SwitchExpr) n);
		else if (n instanceof TextBlockLiteralExpr)
			return v.visit((TextBlockLiteralExpr) n);
		else if (n instanceof YieldStmt)
			return v.visit((YieldStmt) n);
		else if (n instanceof TypePatternExpr)
			return v.visit((TypePatternExpr) n);
		else if (n instanceof RecordPatternExpr)
			return v.visit((RecordPatternExpr) n);
		return true;
	}

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
