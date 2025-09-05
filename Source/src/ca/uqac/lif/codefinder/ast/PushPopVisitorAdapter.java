package ca.uqac.lif.codefinder.ast;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.CompactConstructorDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.ReceiverParameter;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.RecordPatternExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.SwitchExpr;
import com.github.javaparser.ast.expr.TextBlockLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.expr.TypePatternExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.modules.ModuleDeclaration;
import com.github.javaparser.ast.modules.ModuleExportsDirective;
import com.github.javaparser.ast.modules.ModuleOpensDirective;
import com.github.javaparser.ast.modules.ModuleProvidesDirective;
import com.github.javaparser.ast.modules.ModuleRequiresDirective;
import com.github.javaparser.ast.modules.ModuleUsesDirective;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.LocalRecordDeclarationStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.UnparsableStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.stmt.YieldStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VarType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;

public class PushPopVisitorAdapter implements PushPopVisitor
{

	@Override
	public boolean leave(@SuppressWarnings("rawtypes") NodeList n)
	{

		return true;
	}

	@Override
	public boolean leave(AnnotationDeclaration n)
	{

		return true;
	}

	@Override
	public boolean leave(AnnotationMemberDeclaration n)
	{

		return true;
	}

	@Override
	public boolean leave(ArrayAccessExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(ArrayCreationExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(ArrayCreationLevel n)
	{

		return true;
	}

	@Override
	public boolean leave(ArrayInitializerExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(ArrayType n)
	{

		return true;
	}

	@Override
	public boolean leave(AssertStmt n)
	{

		return true;
	}

	@Override
	public boolean leave(AssignExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(BinaryExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(BlockComment n)
	{

		return true;
	}

	@Override
	public boolean leave(BlockStmt n)
	{

		return true;
	}

	@Override
	public boolean leave(BooleanLiteralExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(BreakStmt n)
	{

		return true;
	}

	@Override
	public boolean leave(CastExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(CatchClause n)
	{

		return true;
	}

	@Override
	public boolean leave(CharLiteralExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(ClassExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(ClassOrInterfaceDeclaration n)
	{

		return true;
	}

	@Override
	public boolean leave(ClassOrInterfaceType n)
	{

		return true;
	}

	@Override
	public boolean leave(CompilationUnit n)
	{

		return true;
	}

	@Override
	public boolean leave(ConditionalExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(ConstructorDeclaration n)
	{

		return true;
	}

	@Override
	public boolean leave(ContinueStmt n)
	{

		return true;
	}

	@Override
	public boolean leave(DoStmt n)
	{

		return true;
	}

	@Override
	public boolean leave(DoubleLiteralExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(EmptyStmt n)
	{

		return true;
	}

	@Override
	public boolean leave(EnclosedExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(EnumConstantDeclaration n)
	{

		return true;
	}

	@Override
	public boolean leave(EnumDeclaration n)
	{

		return true;
	}

	@Override
	public boolean leave(ExplicitConstructorInvocationStmt n)
	{

		return true;
	}

	@Override
	public boolean leave(ExpressionStmt n)
	{

		return true;
	}

	@Override
	public boolean leave(FieldAccessExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(FieldDeclaration n)
	{

		return true;
	}

	@Override
	public boolean leave(ForStmt n)
	{

		return true;
	}

	@Override
	public boolean leave(ForEachStmt n)
	{

		return true;
	}

	@Override
	public boolean leave(IfStmt n)
	{

		return true;
	}

	@Override
	public boolean leave(ImportDeclaration n)
	{

		return true;
	}

	@Override
	public boolean leave(InitializerDeclaration n)
	{

		return true;
	}

	@Override
	public boolean leave(InstanceOfExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(IntegerLiteralExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(IntersectionType n)
	{

		return true;
	}

	@Override
	public boolean leave(JavadocComment n)
	{

		return true;
	}

	@Override
	public boolean leave(LabeledStmt n)
	{

		return true;
	}

	@Override
	public boolean leave(LambdaExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(LineComment n)
	{

		return true;
	}

	@Override
	public boolean leave(LocalClassDeclarationStmt n)
	{

		return true;
	}

	@Override
	public boolean leave(LocalRecordDeclarationStmt n)
	{

		return true;
	}

	@Override
	public boolean leave(LongLiteralExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(MarkerAnnotationExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(MemberValuePair n)
	{

		return true;
	}

	@Override
	public boolean leave(MethodCallExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(MethodDeclaration n)
	{

		return true;
	}

	@Override
	public boolean leave(MethodReferenceExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(NameExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(Name n)
	{

		return true;
	}

	@Override
	public boolean leave(NormalAnnotationExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(NullLiteralExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(ObjectCreationExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(PackageDeclaration n)
	{

		return true;
	}

	@Override
	public boolean leave(Parameter n)
	{

		return true;
	}

	@Override
	public boolean leave(PrimitiveType n)
	{

		return true;
	}

	@Override
	public boolean leave(RecordDeclaration n)
	{

		return true;
	}

	@Override
	public boolean leave(CompactConstructorDeclaration n)
	{

		return true;
	}

	@Override
	public boolean leave(ReturnStmt n)
	{

		return true;
	}

	@Override
	public boolean leave(SimpleName n)
	{

		return true;
	}

	@Override
	public boolean leave(SingleMemberAnnotationExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(StringLiteralExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(SuperExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(SwitchEntry n)
	{

		return true;
	}

	@Override
	public boolean leave(SwitchStmt n)
	{

		return true;
	}

	@Override
	public boolean leave(SynchronizedStmt n)
	{

		return true;
	}

	@Override
	public boolean leave(ThisExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(ThrowStmt n)
	{

		return true;
	}

	@Override
	public boolean leave(TryStmt n)
	{

		return true;
	}

	@Override
	public boolean leave(TypeExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(TypeParameter n)
	{

		return true;
	}

	@Override
	public boolean leave(UnaryExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(UnionType n)
	{

		return true;
	}

	@Override
	public boolean leave(UnknownType n)
	{

		return true;
	}

	@Override
	public boolean leave(VariableDeclarationExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(VariableDeclarator n)
	{

		return true;
	}

	@Override
	public boolean leave(VoidType n)
	{

		return true;
	}

	@Override
	public boolean leave(WhileStmt n)
	{

		return true;
	}

	@Override
	public boolean leave(WildcardType n)
	{

		return true;
	}

	@Override
	public boolean leave(ModuleDeclaration n)
	{

		return true;
	}

	@Override
	public boolean leave(ModuleRequiresDirective n)
	{

		return true;
	}

	@Override
	public boolean leave(ModuleExportsDirective n)
	{

		return true;
	}

	@Override
	public boolean leave(ModuleProvidesDirective n)
	{

		return true;
	}

	@Override
	public boolean leave(ModuleUsesDirective n)
	{

		return true;
	}

	@Override
	public boolean leave(ModuleOpensDirective n)
	{

		return true;
	}

	@Override
	public boolean leave(UnparsableStmt n)
	{

		return true;
	}

	@Override
	public boolean leave(ReceiverParameter n)
	{

		return true;
	}

	@Override
	public boolean leave(VarType n)
	{

		return true;
	}

	@Override
	public boolean leave(Modifier n)
	{

		return true;
	}

	@Override
	public boolean leave(SwitchExpr switchExpr)
	{

		return true;
	}

	@Override
	public boolean leave(TextBlockLiteralExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(YieldStmt yieldStmt)
	{

		return true;
	}

	@Override
	public boolean leave(TypePatternExpr n)
	{

		return true;
	}

	@Override
	public boolean leave(RecordPatternExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(@SuppressWarnings("rawtypes") NodeList n)
	{

		return true;
	}

	@Override
	public boolean visit(AnnotationDeclaration n)
	{

		return true;
	}

	@Override
	public boolean visit(AnnotationMemberDeclaration n)
	{

		return true;
	}

	@Override
	public boolean visit(ArrayAccessExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(ArrayCreationExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(ArrayCreationLevel n)
	{

		return true;
	}

	@Override
	public boolean visit(ArrayInitializerExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(ArrayType n)
	{

		return true;
	}

	@Override
	public boolean visit(AssertStmt n)
	{

		return true;
	}

	@Override
	public boolean visit(AssignExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(BinaryExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(BlockComment n)
	{

		return true;
	}

	@Override
	public boolean visit(BlockStmt n)
	{

		return true;
	}

	@Override
	public boolean visit(BooleanLiteralExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(BreakStmt n)
	{

		return true;
	}

	@Override
	public boolean visit(CastExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(CatchClause n)
	{

		return true;
	}

	@Override
	public boolean visit(CharLiteralExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(ClassExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(ClassOrInterfaceDeclaration n)
	{

		return true;
	}

	@Override
	public boolean visit(ClassOrInterfaceType n)
	{

		return true;
	}

	@Override
	public boolean visit(CompilationUnit n)
	{

		return true;
	}

	@Override
	public boolean visit(ConditionalExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(ConstructorDeclaration n)
	{

		return true;
	}

	@Override
	public boolean visit(ContinueStmt n)
	{

		return true;
	}

	@Override
	public boolean visit(DoStmt n)
	{

		return true;
	}

	@Override
	public boolean visit(DoubleLiteralExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(EmptyStmt n)
	{

		return true;
	}

	@Override
	public boolean visit(EnclosedExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(EnumConstantDeclaration n)
	{

		return true;
	}

	@Override
	public boolean visit(EnumDeclaration n)
	{

		return true;
	}

	@Override
	public boolean visit(ExplicitConstructorInvocationStmt n)
	{

		return true;
	}

	@Override
	public boolean visit(ExpressionStmt n)
	{

		return true;
	}

	@Override
	public boolean visit(FieldAccessExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(FieldDeclaration n)
	{

		return true;
	}

	@Override
	public boolean visit(ForStmt n)
	{

		return true;
	}

	@Override
	public boolean visit(ForEachStmt n)
	{

		return true;
	}

	@Override
	public boolean visit(IfStmt n)
	{

		return true;
	}

	@Override
	public boolean visit(ImportDeclaration n)
	{

		return true;
	}

	@Override
	public boolean visit(InitializerDeclaration n)
	{

		return true;
	}

	@Override
	public boolean visit(InstanceOfExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(IntegerLiteralExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(IntersectionType n)
	{

		return true;
	}

	@Override
	public boolean visit(JavadocComment n)
	{

		return true;
	}

	@Override
	public boolean visit(LabeledStmt n)
	{

		return true;
	}

	@Override
	public boolean visit(LambdaExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(LineComment n)
	{

		return true;
	}

	@Override
	public boolean visit(LocalClassDeclarationStmt n)
	{

		return true;
	}

	@Override
	public boolean visit(LocalRecordDeclarationStmt n)
	{

		return true;
	}

	@Override
	public boolean visit(LongLiteralExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(MarkerAnnotationExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(MemberValuePair n)
	{

		return true;
	}

	@Override
	public boolean visit(MethodCallExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(MethodDeclaration n)
	{

		return true;
	}

	@Override
	public boolean visit(MethodReferenceExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(NameExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(Name n)
	{

		return true;
	}

	@Override
	public boolean visit(NormalAnnotationExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(NullLiteralExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(ObjectCreationExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(PackageDeclaration n)
	{

		return true;
	}

	@Override
	public boolean visit(Parameter n)
	{

		return true;
	}

	@Override
	public boolean visit(PrimitiveType n)
	{

		return true;
	}

	@Override
	public boolean visit(RecordDeclaration n)
	{

		return true;
	}

	@Override
	public boolean visit(CompactConstructorDeclaration n)
	{

		return true;
	}

	@Override
	public boolean visit(ReturnStmt n)
	{

		return true;
	}

	@Override
	public boolean visit(SimpleName n)
	{

		return true;
	}

	@Override
	public boolean visit(SingleMemberAnnotationExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(StringLiteralExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(SuperExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(SwitchEntry n)
	{

		return true;
	}

	@Override
	public boolean visit(SwitchStmt n)
	{

		return true;
	}

	@Override
	public boolean visit(SynchronizedStmt n)
	{

		return true;
	}

	@Override
	public boolean visit(ThisExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(ThrowStmt n)
	{

		return true;
	}

	@Override
	public boolean visit(TryStmt n)
	{

		return true;
	}

	@Override
	public boolean visit(TypeExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(TypeParameter n)
	{

		return true;
	}

	@Override
	public boolean visit(UnaryExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(UnionType n)
	{

		return true;
	}

	@Override
	public boolean visit(UnknownType n)
	{

		return true;
	}

	@Override
	public boolean visit(VariableDeclarationExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(VariableDeclarator n)
	{

		return true;
	}

	@Override
	public boolean visit(VoidType n)
	{

		return true;
	}

	@Override
	public boolean visit(WhileStmt n)
	{

		return true;
	}

	@Override
	public boolean visit(WildcardType n)
	{

		return true;
	}

	@Override
	public boolean visit(ModuleDeclaration n)
	{

		return true;
	}

	@Override
	public boolean visit(ModuleRequiresDirective n)
	{

		return true;
	}

	@Override
	public boolean visit(ModuleExportsDirective n)
	{

		return true;
	}

	@Override
	public boolean visit(ModuleProvidesDirective n)
	{

		return true;
	}

	@Override
	public boolean visit(ModuleUsesDirective n)
	{

		return true;
	}

	@Override
	public boolean visit(ModuleOpensDirective n)
	{

		return true;
	}

	@Override
	public boolean visit(UnparsableStmt n)
	{

		return true;
	}

	@Override
	public boolean visit(ReceiverParameter n)
	{

		return true;
	}

	@Override
	public boolean visit(VarType n)
	{

		return true;
	}

	@Override
	public boolean visit(Modifier n)
	{

		return true;
	}

	@Override
	public boolean visit(SwitchExpr switchExpr)
	{

		return true;
	}

	@Override
	public boolean visit(TextBlockLiteralExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(YieldStmt yieldStmt)
	{

		return true;
	}

	@Override
	public boolean visit(TypePatternExpr n)
	{

		return true;
	}

	@Override
	public boolean visit(RecordPatternExpr n)
	{

		return true;
	}
}
