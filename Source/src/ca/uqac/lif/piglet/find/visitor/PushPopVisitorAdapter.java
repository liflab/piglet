package ca.uqac.lif.piglet.find.visitor;

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
	protected boolean m_shouldStop = false;
	
	protected void visit(com.github.javaparser.ast.Node n)
	{
		// Default implementation does nothing
	}
	@Override
	public void leave(@SuppressWarnings("rawtypes") NodeList n) {
		
	}
	@Override
	public void leave(AnnotationDeclaration n) {
		
	}
	@Override
	public void leave(AnnotationMemberDeclaration n) {
		
	}
	@Override
	public void leave(ArrayAccessExpr n) {
		
	}
	@Override
	public void leave(ArrayCreationExpr n) {
		
	}
	@Override
	public void leave(ArrayCreationLevel n) {
		
	}
	@Override
	public void leave(ArrayInitializerExpr n) {
		
	}
	@Override
	public void leave(ArrayType n) {
		
	}
	@Override
	public void leave(AssertStmt n) {
		
	}
	@Override
	public void leave(AssignExpr n) {
		
	}
	@Override
	public void leave(BinaryExpr n) {
		
	}
	@Override
	public void leave(BlockComment n) {
		
	}
	@Override
	public void leave(BlockStmt n) {
		
	}
	@Override
	public void leave(BooleanLiteralExpr n) {
		
	}
	@Override
	public void leave(BreakStmt n) {
		
	}
	@Override
	public void leave(CastExpr n) {
		
	}
	@Override
	public void leave(CatchClause n) {
		
	}
	@Override
	public void leave(CharLiteralExpr n) {
		
	}
	@Override
	public void leave(ClassExpr n) {
		
	}
	@Override
	public void leave(ClassOrInterfaceDeclaration n) {
		
	}
	@Override
	public void leave(ClassOrInterfaceType n) {
		
	}
	@Override
	public void leave(CompilationUnit n) {
		
	}
	@Override
	public void leave(ConditionalExpr n) {
		
	}
	@Override
	public void leave(ConstructorDeclaration n) {
		
	}
	@Override
	public void leave(ContinueStmt n) {
		
	}
	@Override
	public void leave(DoStmt n) {
		
	}
	@Override
	public void leave(DoubleLiteralExpr n) {
		
	}
	@Override
	public void leave(EmptyStmt n) {
		
	}
	@Override
	public void leave(EnclosedExpr n) {
		
	}
	@Override
	public void leave(EnumConstantDeclaration n) {
		
	}
	@Override
	public void leave(EnumDeclaration n) {
		
	}
	@Override
	public void leave(ExplicitConstructorInvocationStmt n) {
		
	}
	@Override
	public void leave(ExpressionStmt n) {
		
	}
	@Override
	public void leave(FieldAccessExpr n) {
		
	}
	@Override
	public void leave(FieldDeclaration n) {
		
	}
	@Override
	public void leave(ForStmt n) {
		
	}
	@Override
	public void leave(ForEachStmt n) {
		
	}
	@Override
	public void leave(IfStmt n) {
		
	}
	@Override
	public void leave(ImportDeclaration n) {
		
	}
	@Override
	public void leave(InitializerDeclaration n) {
		
	}
	@Override
	public void leave(InstanceOfExpr n) {
		
	}
	@Override
	public void leave(IntegerLiteralExpr n) {
		
	}
	@Override
	public void leave(IntersectionType n) {
		
	}
	@Override
	public void leave(JavadocComment n) {
		
	}
	@Override
	public void leave(LabeledStmt n) {
		
	}
	@Override
	public void leave(LambdaExpr n) {
		
	}
	@Override
	public void leave(LineComment n) {
		
	}
	@Override
	public void leave(LocalClassDeclarationStmt n) {
		
	}
	@Override
	public void leave(LocalRecordDeclarationStmt n) {
		
	}
	@Override
	public void leave(LongLiteralExpr n) {
		
	}
	@Override
	public void leave(MarkerAnnotationExpr n) {
		
	}
	@Override
	public void leave(MemberValuePair n) {
		
	}
	@Override
	public void leave(MethodCallExpr n) {
		
	}
	@Override
	public void leave(MethodDeclaration n) {
		
	}
	@Override
	public void leave(MethodReferenceExpr n) {
		
	}
	@Override
	public void leave(NameExpr n) {
		
	}
	@Override
	public void leave(Name n) {
		
	}
	@Override
	public void leave(NormalAnnotationExpr n) {
		
	}
	@Override
	public void leave(NullLiteralExpr n) {
		
	}
	@Override
	public void leave(ObjectCreationExpr n) {
		
	}
	@Override
	public void leave(PackageDeclaration n) {
		
	}
	@Override
	public void leave(Parameter n) {
		
	}
	@Override
	public void leave(PrimitiveType n) {
		
	}
	@Override
	public void leave(RecordDeclaration n) {
		
	}
	@Override
	public void leave(CompactConstructorDeclaration n) {
		
	}
	@Override
	public void leave(ReturnStmt n) {
		
	}
	@Override
	public void leave(SimpleName n) {
		
	}
	@Override
	public void leave(SingleMemberAnnotationExpr n) {
		
	}
	@Override
	public void leave(StringLiteralExpr n) {
		
	}
	@Override
	public void leave(SuperExpr n) {
		
	}
	@Override
	public void leave(SwitchEntry n) {
		
	}
	@Override
	public void leave(SwitchStmt n) {
		
	}
	@Override
	public void leave(SynchronizedStmt n) {
		
	}
	@Override
	public void leave(ThisExpr n) {
		
	}
	@Override
	public void leave(ThrowStmt n) {
		
	}
	@Override
	public void leave(TryStmt n) {
		
	}
	@Override
	public void leave(TypeExpr n) {
		
	}
	@Override
	public void leave(TypeParameter n) {
		
	}
	@Override
	public void leave(UnaryExpr n) {
		
	}
	@Override
	public void leave(UnionType n) {
		
	}
	@Override
	public void leave(UnknownType n) {
		
	}
	@Override
	public void leave(VariableDeclarationExpr n) {
		
	}
	@Override
	public void leave(VariableDeclarator n) {
		
	}
	@Override
	public void leave(VoidType n) {
		
	}
	@Override
	public void leave(WhileStmt n) {
		
	}
	@Override
	public void leave(WildcardType n) {
		
	}
	@Override
	public void leave(ModuleDeclaration n) {
		
	}
	@Override
	public void leave(ModuleRequiresDirective n) {
		
	}
	@Override
	public void leave(ModuleExportsDirective n) {
		
	}
	@Override
	public void leave(ModuleProvidesDirective n) {
		
	}
	@Override
	public void leave(ModuleUsesDirective n) {
		
	}
	@Override
	public void leave(ModuleOpensDirective n) {
		
	}
	@Override
	public void leave(UnparsableStmt n) {
		
	}
	@Override
	public void leave(ReceiverParameter n) {
		
	}
	@Override
	public void leave(VarType n) {
		
	}
	@Override
	public void leave(Modifier n) {
		
	}
	@Override
	public void leave(SwitchExpr switchExpr) {
		leave(switchExpr);
	}
	@Override
	public void leave(TextBlockLiteralExpr n) {
		
	}
	@Override
	public void leave(YieldStmt yieldStmt) {
		leave(yieldStmt);
	}
	@Override
	public void leave(TypePatternExpr n) {
		
	}
	@Override
	public void leave(RecordPatternExpr n) {
		
	}
	@Override
	public void visit(@SuppressWarnings("rawtypes") NodeList n)
	{ 

		
	}

	@Override
	public void visit(AnnotationDeclaration n)
	{ 

		
	}

	@Override
	public void visit(AnnotationMemberDeclaration n)
	{ 

		
	}

	@Override
	public void visit(ArrayAccessExpr n)
	{ 

		
	}

	@Override
	public void visit(ArrayCreationExpr n)
	{ 

		
	}

	@Override
	public void visit(ArrayCreationLevel n)
	{ 

		
	}

	@Override
	public void visit(ArrayInitializerExpr n)
	{ 

		
	}

	@Override
	public void visit(ArrayType n)
	{ 

		
	}

	@Override
	public void visit(AssertStmt n)
	{ 

		
	}

	@Override
	public void visit(AssignExpr n)
	{ 

		
	}

	@Override
	public void visit(BinaryExpr n)
	{ 

		
	}

	@Override
	public void visit(BlockComment n)
	{ 

		
	}

	@Override
	public void visit(BlockStmt n)
	{ 

		
	}

	@Override
	public void visit(BooleanLiteralExpr n)
	{ 

		
	}

	@Override
	public void visit(BreakStmt n)
	{ 

		
	}

	@Override
	public void visit(CastExpr n)
	{ 

		
	}

	@Override
	public void visit(CatchClause n)
	{ 

		
	}

	@Override
	public void visit(CharLiteralExpr n)
	{ 

		
	}

	@Override
	public void visit(ClassExpr n)
	{ 

		
	}

	@Override
	public void visit(ClassOrInterfaceDeclaration n)
	{ 

		
	}

	@Override
	public void visit(ClassOrInterfaceType n)
	{ 

		
	}

	@Override
	public void visit(CompilationUnit n)
	{ 

		
	}

	@Override
	public void visit(ConditionalExpr n)
	{ 

		
	}

	@Override
	public void visit(ConstructorDeclaration n)
	{ 

		
	}

	@Override
	public void visit(ContinueStmt n)
	{ 

		
	}

	@Override
	public void visit(DoStmt n)
	{ 

		
	}

	@Override
	public void visit(DoubleLiteralExpr n)
	{ 

		
	}

	@Override
	public void visit(EmptyStmt n)
	{ 

		
	}

	@Override
	public void visit(EnclosedExpr n)
	{ 

		
	}

	@Override
	public void visit(EnumConstantDeclaration n)
	{ 

		
	}

	@Override
	public void visit(EnumDeclaration n)
	{ 

		
	}

	@Override
	public void visit(ExplicitConstructorInvocationStmt n)
	{ 

		
	}

	@Override
	public void visit(ExpressionStmt n)
	{ 

		
	}

	@Override
	public void visit(FieldAccessExpr n)
	{ 

		
	}

	@Override
	public void visit(FieldDeclaration n)
	{ 

		
	}

	@Override
	public void visit(ForStmt n)
	{ 

		
	}

	@Override
	public void visit(ForEachStmt n)
	{ 

		
	}

	@Override
	public void visit(IfStmt n)
	{ 

		
	}

	@Override
	public void visit(ImportDeclaration n)
	{ 

		
	}

	@Override
	public void visit(InitializerDeclaration n)
	{ 

		
	}

	@Override
	public void visit(InstanceOfExpr n)
	{ 

		
	}

	@Override
	public void visit(IntegerLiteralExpr n)
	{ 

		
	}

	@Override
	public void visit(IntersectionType n)
	{ 

		
	}

	@Override
	public void visit(JavadocComment n)
	{ 

		
	}

	@Override
	public void visit(LabeledStmt n)
	{ 

		
	}

	@Override
	public void visit(LambdaExpr n)
	{ 

		
	}

	@Override
	public void visit(LineComment n)
	{ 

		
	}

	@Override
	public void visit(LocalClassDeclarationStmt n)
	{ 

		
	}

	@Override
	public void visit(LocalRecordDeclarationStmt n)
	{ 

		
	}

	@Override
	public void visit(LongLiteralExpr n)
	{ 

		
	}

	@Override
	public void visit(MarkerAnnotationExpr n)
	{ 

		
	}

	@Override
	public void visit(MemberValuePair n)
	{ 

		
	}

	@Override
	public void visit(MethodCallExpr n)
	{ 

		
	}

	@Override
	public void visit(MethodDeclaration n)
	{ 

		
	}

	@Override
	public void visit(MethodReferenceExpr n)
	{ 

		
	}

	@Override
	public void visit(NameExpr n)
	{ 

		
	}

	@Override
	public void visit(Name n)
	{ 

		
	}

	@Override
	public void visit(NormalAnnotationExpr n)
	{ 

		
	}

	@Override
	public void visit(NullLiteralExpr n)
	{ 

		
	}

	@Override
	public void visit(ObjectCreationExpr n)
	{ 

		
	}

	@Override
	public void visit(PackageDeclaration n)
	{ 

		
	}

	@Override
	public void visit(Parameter n)
	{ 

		
	}

	@Override
	public void visit(PrimitiveType n)
	{ 

		
	}

	@Override
	public void visit(RecordDeclaration n)
	{ 

		
	}

	@Override
	public void visit(CompactConstructorDeclaration n)
	{ 

		
	}

	@Override
	public void visit(ReturnStmt n)
	{ 

		
	}

	@Override
	public void visit(SimpleName n)
	{ 

		
	}

	@Override
	public void visit(SingleMemberAnnotationExpr n)
	{ 

		
	}

	@Override
	public void visit(StringLiteralExpr n)
	{ 

		
	}

	@Override
	public void visit(SuperExpr n)
	{ 

		
	}

	@Override
	public void visit(SwitchEntry n)
	{ 

		
	}

	@Override
	public void visit(SwitchStmt n)
	{ 

		
	}

	@Override
	public void visit(SynchronizedStmt n)
	{ 

		
	}

	@Override
	public void visit(ThisExpr n)
	{ 

		
	}

	@Override
	public void visit(ThrowStmt n)
	{ 

		
	}

	@Override
	public void visit(TryStmt n)
	{ 

		
	}

	@Override
	public void visit(TypeExpr n)
	{ 

		
	}

	@Override
	public void visit(TypeParameter n)
	{ 

		
	}

	@Override
	public void visit(UnaryExpr n)
	{ 

		
	}

	@Override
	public void visit(UnionType n)
	{ 

		
	}

	@Override
	public void visit(UnknownType n)
	{ 

		
	}

	@Override
	public void visit(VariableDeclarationExpr n)
	{ 

		
	}

	@Override
	public void visit(VariableDeclarator n)
	{ 

		
	}

	@Override
	public void visit(VoidType n)
	{ 

		
	}

	@Override
	public void visit(WhileStmt n)
	{ 

		
	}

	@Override
	public void visit(WildcardType n)
	{ 

		
	}

	@Override
	public void visit(ModuleDeclaration n)
	{ 

		
	}

	@Override
	public void visit(ModuleRequiresDirective n)
	{ 

		
	}

	@Override
	public void visit(ModuleExportsDirective n)
	{ 

		
	}

	@Override
	public void visit(ModuleProvidesDirective n)
	{ 

		
	}

	@Override
	public void visit(ModuleUsesDirective n)
	{ 

		
	}

	@Override
	public void visit(ModuleOpensDirective n)
	{ 

		
	}

	@Override
	public void visit(UnparsableStmt n)
	{ 

		
	}

	@Override
	public void visit(ReceiverParameter n)
	{ 

		
	}

	@Override
	public void visit(VarType n)
	{ 

		
	}

	@Override
	public void visit(Modifier n)
	{ 

		
	}

	@Override
	public void visit(SwitchExpr switchExpr)
	{

		
	}

	@Override
	public void visit(TextBlockLiteralExpr n)
	{ 

		
	}

	@Override
	public void visit(YieldStmt yieldStmt)
	{

		
	}

	@Override
	public void visit(TypePatternExpr n)
	{ 

		
	}

	@Override
	public void visit(RecordPatternExpr n)
	{ 

	}
	
	@Override
	public boolean shouldStop()
	{
		return m_shouldStop;
	}
	
	@Override
	public void stop()
  {
		m_shouldStop = true;
  }
	
	public void reset()
	{
		m_shouldStop = false;
	}
}
