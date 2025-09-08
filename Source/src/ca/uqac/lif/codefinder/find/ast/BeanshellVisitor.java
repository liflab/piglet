package ca.uqac.lif.codefinder.find.ast;

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

import ca.uqac.lif.codefinder.find.TokenFinderContext;

/**
 * A visitor with empty visit and leave methods for all node types.
 */
public abstract class BeanshellVisitor extends AstAssertionFinder
{
	public BeanshellVisitor(String name)
	{
		this(name, null);
	}

	protected BeanshellVisitor(String name, TokenFinderContext context)
	{
		super(name, context);
	}

	@Override
	public boolean leave(@SuppressWarnings("rawtypes") NodeList n)
	{
		return leaveNodeList(n);

	}

	protected boolean leaveNodeList(@SuppressWarnings("rawtypes") NodeList n)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean leave(AnnotationDeclaration n)
	{
		return leaveAnnotationDeclaration(n);

	}

	protected boolean leaveAnnotationDeclaration(AnnotationDeclaration n)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean leave(AnnotationMemberDeclaration n)
	{
		return leaveAnnotationMemberDeclaration(n);

	}

	protected boolean leaveAnnotationMemberDeclaration(AnnotationMemberDeclaration n)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean leave(ArrayAccessExpr n)
	{
		return leaveArrayAccessExpr(n);

	}

	protected boolean leaveArrayAccessExpr(ArrayAccessExpr n)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean leave(ArrayCreationExpr n)
	{
		return leaveArrayCreationExpr(n);

	}

	protected boolean leaveArrayCreationExpr(ArrayCreationExpr n)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean leave(ArrayCreationLevel n)
	{
		return leaveArrayCreationLevel(n);

	}

	protected boolean leaveArrayCreationLevel(ArrayCreationLevel n)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean leave(ArrayInitializerExpr n)
	{
		return leaveArrayInitializerExpr(n);

	}

	protected boolean leaveArrayInitializerExpr(ArrayInitializerExpr n)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean leave(ArrayType n)
	{
		return leaveArrayType(n);
	}

	protected boolean leaveArrayType(ArrayType n)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean leave(AssertStmt n)
	{
		return leaveAssertStmt(n);

	}

	protected boolean leaveAssertStmt(AssertStmt n)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean leave(AssignExpr n)
	{
		return leaveAssignExpr(n);

	}

	protected boolean leaveAssignExpr(AssignExpr n)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean leave(BinaryExpr n)
	{
		return leaveBinaryExpr(n);

	}

	@Override
	public boolean leave(BlockComment n)
	{
		return leaveBlockComment(n);

	}

	@Override
	public boolean leave(BlockStmt n)
	{
		return leaveBlockStmt(n);

	}

	@Override
	public boolean leave(BooleanLiteralExpr n)
	{
		return leaveBooleanLiteralExpr(n);

	}

	@Override
	public boolean leave(BreakStmt n)
	{
		return leaveBreakStmt(n);

	}

	@Override
	public boolean leave(CastExpr n)
	{
		return leaveCastExpr(n);

	}

	@Override
	public boolean leave(CatchClause n)
	{
		return leaveCatchClause(n);

	}

	@Override
	public boolean leave(CharLiteralExpr n)
	{
		return leaveCharLiteralExpr(n);

	}

	@Override
	public boolean leave(ClassExpr n)
	{
		return leaveClassExpr(n);

	}

	@Override
	public boolean leave(ClassOrInterfaceDeclaration n)
	{
		return leaveClassOrInterfaceDeclaration(n);

	}

	@Override
	public boolean leave(ClassOrInterfaceType n)
	{
		return leaveClassOrInterfaceType(n);

	}

	@Override
	public boolean leave(CompilationUnit n)
	{
		return leaveCompilationUnit(n);

	}

	@Override
	public boolean leave(ConditionalExpr n)
	{
		return leaveConditionalExpr(n);

	}

	@Override
	public boolean leave(ConstructorDeclaration n)
	{
		return leaveConstructorDeclaration(n);

	}

	@Override
	public boolean leave(ContinueStmt n)
	{
		return leaveContinueStmt(n);

	}

	@Override
	public boolean leave(DoStmt n)
	{
		return leaveDoStmt(n);

	}

	@Override
	public boolean leave(DoubleLiteralExpr n)
	{
		return leaveDoubleLiteralExpr(n);

	}

	@Override
	public boolean leave(EmptyStmt n)
	{
		return leaveEmptyStmt(n);

	}

	@Override
	public boolean leave(EnclosedExpr n)
	{
		return leaveEnclosedExpr(n);

	}

	@Override
	public boolean leave(EnumConstantDeclaration n)
	{
		return leaveEnumConstantDeclaration(n);

	}

	@Override
	public boolean leave(EnumDeclaration n)
	{
		return leaveEnumDeclaration(n);

	}

	@Override
	public boolean leave(ExplicitConstructorInvocationStmt n)
	{
		return leaveExplicitConstructorInvocationStmt(n);

	}

	@Override
	public boolean leave(ExpressionStmt n)
	{
		return leaveExpressionStmt(n);

	}

	@Override
	public boolean leave(FieldAccessExpr n)
	{
		return leaveFieldAccessExpr(n);

	}

	@Override
	public boolean leave(FieldDeclaration n)
	{
		return leaveFieldDeclaration(n);

	}

	@Override
	public boolean leave(ForStmt n)
	{
		return leaveForStmt(n);

	}

	@Override
	public boolean leave(ForEachStmt n)
	{
		return leaveForEachStmt(n);

	}

	@Override
	public boolean leave(IfStmt n)
	{
		return leaveIfStmt(n);

	}

	@Override
	public boolean leave(ImportDeclaration n)
	{
		return leaveImportDeclaration(n);

	}

	@Override
	public boolean leave(InitializerDeclaration n)
	{
		return leaveInitializerDeclaration(n);

	}

	@Override
	public boolean leave(InstanceOfExpr n)
	{
		return leaveInstanceOfExpr(n);

	}

	@Override
	public boolean leave(IntegerLiteralExpr n)
	{
		return leaveIntegerLiteralExpr(n);

	}

	@Override
	public boolean leave(IntersectionType n)
	{
		return leaveIntersectionType(n);

	}

	@Override
	public boolean leave(JavadocComment n)
	{
		return leaveJavadocComment(n);

	}

	@Override
	public boolean leave(LabeledStmt n)
	{
		return leaveLabeledStmt(n);

	}

	@Override
	public boolean leave(LambdaExpr n)
	{
		return leaveLambdaExpr(n);

	}

	@Override
	public boolean leave(LineComment n)
	{
		return leaveLineComment(n);

	}

	@Override
	public boolean leave(LocalClassDeclarationStmt n)
	{
		return leaveLocalClassDeclarationStmt(n);

	}

	@Override
	public boolean leave(LocalRecordDeclarationStmt n)
	{
		return leaveLocalRecordDeclarationStmt(n);

	}

	@Override
	public boolean leave(LongLiteralExpr n)
	{
		return leaveLongLiteralExpr(n);

	}

	@Override
	public boolean leave(MarkerAnnotationExpr n)
	{
		return leaveMarkerAnnotationExpr(n);

	}

	@Override
	public boolean leave(MemberValuePair n)
	{
		return leaveMemberValuePair(n);

	}

	@Override
	public boolean leave(MethodCallExpr n)
	{
		return leaveMethodCallExpr(n);

	}

	@Override
	public boolean leave(MethodDeclaration n)
	{
		return leaveMethodDeclaration(n);

	}

	@Override
	public boolean leave(MethodReferenceExpr n)
	{
		return leaveMethodReferenceExpr(n);

	}

	@Override
	public boolean leave(NameExpr n)
	{
		return leaveNameExpr(n);

	}

	@Override
	public boolean leave(Name n)
	{
		return leaveName(n);

	}

	@Override
	public boolean leave(NormalAnnotationExpr n)
	{
		return leaveNormalAnnotationExpr(n);

	}

	@Override
	public boolean leave(NullLiteralExpr n)
	{
		return leaveNullLiteralExpr(n);

	}

	@Override
	public boolean leave(ObjectCreationExpr n)
	{
		return leaveObjectCreationExpr(n);

	}

	@Override
	public boolean leave(PackageDeclaration n)
	{
		return leavePackageDeclaration(n);

	}

	@Override
	public boolean leave(Parameter n)
	{
		return leaveParameter(n);

	}

	@Override
	public boolean leave(PrimitiveType n)
	{
		return leavePrimitiveType(n);

	}

	@Override
	public boolean leave(RecordDeclaration n)
	{
		return leaveRecordDeclaration(n);

	}

	@Override
	public boolean leave(CompactConstructorDeclaration n)
	{
		return leaveCompactConstructorDeclaration(n);

	}

	@Override
	public boolean leave(ReturnStmt n)
	{
		return leaveReturnStmt(n);

	}

	@Override
	public boolean leave(SimpleName n)
	{
		return leaveSimpleName(n);

	}

	@Override
	public boolean leave(SingleMemberAnnotationExpr n)
	{
		return leaveSingleMemberAnnotationExpr(n);

	}

	@Override
	public boolean leave(StringLiteralExpr n)
	{
		return leaveStringLiteralExpr(n);

	}

	@Override
	public boolean leave(SuperExpr n)
	{
		return leaveSuperExpr(n);

	}

	@Override
	public boolean leave(SwitchEntry n)
	{
		return leaveSwitchEntry(n);

	}

	@Override
	public boolean leave(SwitchStmt n)
	{
		return leaveSwitchStmt(n);

	}

	@Override
	public boolean leave(SynchronizedStmt n)
	{
		return leaveSynchronizedStmt(n);

	}

	@Override
	public boolean leave(ThisExpr n)
	{
		return leaveThisExpr(n);

	}

	@Override
	public boolean leave(ThrowStmt n)
	{
		return leaveThrowStmt(n);

	}

	@Override
	public boolean leave(TryStmt n)
	{
		return leaveTryStmt(n);

	}

	@Override
	public boolean leave(TypeExpr n)
	{
		return leaveTypeExpr(n);

	}

	@Override
	public boolean leave(TypeParameter n)
	{
		return leaveTypeParameter(n);

	}

	@Override
	public boolean leave(UnaryExpr n)
	{
		return leaveUnaryExpr(n);

	}

	@Override
	public boolean leave(UnionType n)
	{
		return leaveUnionType(n);

	}

	@Override
	public boolean leave(UnknownType n)
	{
		return leaveUnknownType(n);

	}

	@Override
	public boolean leave(VariableDeclarationExpr n)
	{
		return leaveVariableDeclarationExpr(n);

	}

	@Override
	public boolean leave(VariableDeclarator n)
	{
		return leaveVariableDeclarator(n);

	}

	@Override
	public boolean leave(VoidType n)
	{
		return leaveVoidType(n);

	}

	@Override
	public boolean leave(WhileStmt n)
	{
		return leaveWhileStmt(n);

	}

	@Override
	public boolean leave(WildcardType n)
	{
		return leaveWildcardType(n);

	}

	@Override
	public boolean leave(ModuleDeclaration n)
	{
		return leaveModuleDeclaration(n);

	}

	@Override
	public boolean leave(ModuleRequiresDirective n)
	{
		return leaveModuleRequiresDirective(n);

	}

	@Override
	public boolean leave(ModuleExportsDirective n)
	{
		return leaveModuleExportsDirective(n);

	}

	@Override
	public boolean leave(ModuleProvidesDirective n)
	{
		return leaveModuleProvidesDirective(n);

	}

	@Override
	public boolean leave(ModuleUsesDirective n)
	{
		return leaveModuleUsesDirective(n);

	}

	@Override
	public boolean leave(ModuleOpensDirective n)
	{
		return leaveModuleOpensDirective(n);

	}

	@Override
	public boolean leave(UnparsableStmt n)
	{
		return leaveUnparsableStmt(n);

	}

	@Override
	public boolean leave(ReceiverParameter n)
	{
		return leaveReceiverParameter(n);

	}

	@Override
	public boolean leave(VarType n)
	{
		return leaveVarType(n);

	}

	@Override
	public boolean leave(Modifier n)
	{
		return leaveModifier(n);

	}

	@Override
	public boolean leave(SwitchExpr switchExpr)
	{
		return leaveSwitchExpr(switchExpr);

	}

	@Override
	public boolean leave(TextBlockLiteralExpr n)
	{
		return leaveTextBlockLiteralExpr(n);

	}

	@Override
	public boolean leave(YieldStmt yieldStmt)
	{
		return leaveYieldStmt(yieldStmt);

	}

	@Override
	public boolean leave(TypePatternExpr n)
	{
		return leaveTypePatternExpr(n);

	}

	@Override
	public boolean leave(RecordPatternExpr n)
	{
		return leaveRecordPatternExpr(n);

	}

	@Override
	public boolean visit(@SuppressWarnings("rawtypes") NodeList n)
	{
		return visitNodeList(n);

	}

	@Override
	public boolean visit(AnnotationDeclaration n)
	{
		return visitAnnotationDeclaration(n);

	}

	@Override
	public boolean visit(AnnotationMemberDeclaration n)
	{
		return visitAnnotationMemberDeclaration(n);

	}

	@Override
	public boolean visit(ArrayAccessExpr n)
	{
		return visitArrayAccessExpr(n);

	}

	@Override
	public boolean visit(ArrayCreationExpr n)
	{
		return visitArrayCreationExpr(n);

	}

	@Override
	public boolean visit(ArrayCreationLevel n)
	{
		return visitArrayCreationLevel(n);

	}

	@Override
	public boolean visit(ArrayInitializerExpr n)
	{
		return visitArrayInitializerExpr(n);

	}

	@Override
	public boolean visit(ArrayType n)
	{
		return visitArrayType(n);

	}

	@Override
	public boolean visit(AssertStmt n)
	{
		return visitAssertStmt(n);

	}

	@Override
	public boolean visit(AssignExpr n)
	{
		return visitAssignExpr(n);

	}

	@Override
	public boolean visit(BinaryExpr n)
	{
		return visitBinaryExpr(n);

	}

	@Override
	public boolean visit(BlockComment n)
	{
		return visitBlockComment(n);

	}

	@Override
	public boolean visit(BlockStmt n)
	{
		return visitBlockStmt(n);

	}

	@Override
	public boolean visit(BooleanLiteralExpr n)
	{
		return visitBooleanLiteralExpr(n);

	}

	@Override
	public boolean visit(BreakStmt n)
	{
		return visitBreakStmt(n);

	}

	@Override
	public boolean visit(CastExpr n)
	{
		return visitCastExpr(n);

	}

	@Override
	public boolean visit(CatchClause n)
	{
		return visitCatchClause(n);

	}

	@Override
	public boolean visit(CharLiteralExpr n)
	{
		return visitCharLiteralExpr(n);

	}

	@Override
	public boolean visit(ClassExpr n)
	{
		return visitClassExpr(n);

	}

	@Override
	public boolean visit(ClassOrInterfaceDeclaration n)
	{
		return visitClassOrInterfaceDeclaration(n);

	}

	@Override
	public boolean visit(ClassOrInterfaceType n)
	{
		return visitClassOrInterfaceType(n);

	}

	@Override
	public boolean visit(CompilationUnit n)
	{
		return visitCompilationUnit(n);

	}

	@Override
	public boolean visit(ConditionalExpr n)
	{
		return visitConditionalExpr(n);

	}

	@Override
	public boolean visit(ConstructorDeclaration n)
	{
		return visitConstructorDeclaration(n);

	}

	@Override
	public boolean visit(ContinueStmt n)
	{
		return visitContinueStmt(n);

	}

	@Override
	public boolean visit(DoStmt n)
	{
		return visitDoStmt(n);

	}

	@Override
	public boolean visit(DoubleLiteralExpr n)
	{
		return visitDoubleLiteralExpr(n);

	}

	@Override
	public boolean visit(EmptyStmt n)
	{
		return visitEmptyStmt(n);

	}

	@Override
	public boolean visit(EnclosedExpr n)
	{
		return visitEnclosedExpr(n);

	}

	@Override
	public boolean visit(EnumConstantDeclaration n)
	{
		return visitEnumConstantDeclaration(n);

	}

	@Override
	public boolean visit(EnumDeclaration n)
	{
		return visitEnumDeclaration(n);

	}

	@Override
	public boolean visit(ExplicitConstructorInvocationStmt n)
	{
		return visitExplicitConstructorInvocationStmt(n);

	}

	@Override
	public boolean visit(ExpressionStmt n)
	{
		return visitExpressionStmt(n);

	}

	@Override
	public boolean visit(FieldAccessExpr n)
	{
		return visitFieldAccessExpr(n);

	}

	@Override
	public boolean visit(FieldDeclaration n)
	{
		return visitFieldDeclaration(n);

	}

	@Override
	public boolean visit(ForStmt n)
	{
		return visitForStmt(n);

	}

	@Override
	public boolean visit(ForEachStmt n)
	{
		return visitForEachStmt(n);

	}

	@Override
	public boolean visit(IfStmt n)
	{
		return visitIfStmt(n);

	}

	@Override
	public boolean visit(ImportDeclaration n)
	{
		return visitImportDeclaration(n);

	}

	@Override
	public boolean visit(InitializerDeclaration n)
	{
		return visitInitializerDeclaration(n);

	}

	@Override
	public boolean visit(InstanceOfExpr n)
	{
		return visitInstanceOfExpr(n);

	}

	@Override
	public boolean visit(IntegerLiteralExpr n)
	{
		return visitIntegerLiteralExpr(n);

	}

	@Override
	public boolean visit(IntersectionType n)
	{
		return visitIntersectionType(n);

	}

	@Override
	public boolean visit(JavadocComment n)
	{
		return visitJavadocComment(n);

	}

	@Override
	public boolean visit(LabeledStmt n)
	{
		return visitLabeledStmt(n);

	}

	@Override
	public boolean visit(LambdaExpr n)
	{
		return visitLambdaExpr(n);

	}

	@Override
	public boolean visit(LineComment n)
	{
		return visitLineComment(n);

	}

	@Override
	public boolean visit(LocalClassDeclarationStmt n)
	{
		return visitLocalClassDeclarationStmt(n);

	}

	@Override
	public boolean visit(LocalRecordDeclarationStmt n)
	{
		return visitLocalRecordDeclarationStmt(n);

	}

	@Override
	public boolean visit(LongLiteralExpr n)
	{
		return visitLongLiteralExpr(n);

	}

	@Override
	public boolean visit(MarkerAnnotationExpr n)
	{
		return visitMarkerAnnotationExpr(n);

	}

	@Override
	public boolean visit(MemberValuePair n)
	{
		return visitMemberValuePair(n);

	}

	@Override
	public boolean visit(MethodCallExpr n)
	{
		return visitMethodCallExpr(n);

	}

	@Override
	public boolean visit(MethodDeclaration n)
	{
		return visitMethodDeclaration(n);

	}

	@Override
	public boolean visit(MethodReferenceExpr n)
	{
		return visitMethodReferenceExpr(n);

	}

	@Override
	public boolean visit(NameExpr n)
	{
		return visitNameExpr(n);

	}

	@Override
	public boolean visit(Name n)
	{
		return visitName(n);

	}

	@Override
	public boolean visit(NormalAnnotationExpr n)
	{
		return visitNormalAnnotationExpr(n);

	}

	@Override
	public boolean visit(NullLiteralExpr n)
	{
		return visitNullLiteralExpr(n);

	}

	@Override
	public boolean visit(ObjectCreationExpr n)
	{
		return visitObjectCreationExpr(n);

	}

	@Override
	public boolean visit(PackageDeclaration n)
	{
		return visitPackageDeclaration(n);

	}

	@Override
	public boolean visit(Parameter n)
	{
		return visitParameter(n);

	}

	@Override
	public boolean visit(PrimitiveType n)
	{
		return visitPrimitiveType(n);

	}

	@Override
	public boolean visit(RecordDeclaration n)
	{
		return visitRecordDeclaration(n);

	}

	@Override
	public boolean visit(CompactConstructorDeclaration n)
	{
		return visitCompactConstructorDeclaration(n);

	}

	@Override
	public boolean visit(ReturnStmt n)
	{
		return visitReturnStmt(n);

	}

	@Override
	public boolean visit(SimpleName n)
	{
		return visitSimpleName(n);

	}

	@Override
	public boolean visit(SingleMemberAnnotationExpr n)
	{
		return visitSingleMemberAnnotationExpr(n);

	}

	@Override
	public boolean visit(StringLiteralExpr n)
	{
		return visitStringLiteralExpr(n);

	}

	@Override
	public boolean visit(SuperExpr n)
	{
		return visitSuperExpr(n);

	}

	@Override
	public boolean visit(SwitchEntry n)
	{
		return visitSwitchEntry(n);

	}

	@Override
	public boolean visit(SwitchStmt n)
	{
		return visitSwitchStmt(n);

	}

	@Override
	public boolean visit(SynchronizedStmt n)
	{
		return visitSynchronizedStmt(n);

	}

	@Override
	public boolean visit(ThisExpr n)
	{
		return visitThisExpr(n);

	}

	@Override
	public boolean visit(ThrowStmt n)
	{
		return visitThrowStmt(n);

	}

	@Override
	public boolean visit(TryStmt n)
	{
		return visitTryStmt(n);

	}

	@Override
	public boolean visit(TypeExpr n)
	{
		return visitTypeExpr(n);

	}

	@Override
	public boolean visit(TypeParameter n)
	{
		return visitTypeParameter(n);

	}

	@Override
	public boolean visit(UnaryExpr n)
	{
		return visitUnaryExpr(n);

	}

	@Override
	public boolean visit(UnionType n)
	{
		return visitUnionType(n);

	}

	@Override
	public boolean visit(UnknownType n)
	{
		return visitUnknownType(n);

	}

	@Override
	public boolean visit(VariableDeclarationExpr n)
	{
		return visitVariableDeclarationExpr(n);

	}

	@Override
	public boolean visit(VariableDeclarator n)
	{
		return visitVariableDeclarator(n);

	}

	@Override
	public boolean visit(VoidType n)
	{
		return visitVoidType(n);

	}

	@Override
	public boolean visit(WhileStmt n)
	{
		return visitWhileStmt(n);

	}

	@Override
	public boolean visit(WildcardType n)
	{
		return visitWildcardType(n);

	}

	@Override
	public boolean visit(ModuleDeclaration n)
	{
		return visitModuleDeclaration(n);

	}

	@Override
	public boolean visit(ModuleRequiresDirective n)
	{
		return visitModuleRequiresDirective(n);

	}

	@Override
	public boolean visit(ModuleExportsDirective n)
	{
		return visitModuleExportsDirective(n);

	}

	@Override
	public boolean visit(ModuleProvidesDirective n)
	{
		return visitModuleProvidesDirective(n);

	}

	@Override
	public boolean visit(ModuleUsesDirective n)
	{
		return visitModuleUsesDirective(n);

	}

	@Override
	public boolean visit(ModuleOpensDirective n)
	{
		return visitModuleOpensDirective(n);

	}

	@Override
	public boolean visit(UnparsableStmt n)
	{
		return visitUnparsableStmt(n);

	}

	@Override
	public boolean visit(ReceiverParameter n)
	{
		return visitReceiverParameter(n);

	}

	@Override
	public boolean visit(VarType n)
	{
		return visitVarType(n);

	}

	@Override
	public boolean visit(Modifier n)
	{
		return visitModifier(n);

	}

	@Override
	public boolean visit(SwitchExpr switchExpr)
	{
		return visitSwitchExpr(switchExpr);

	}

	@Override
	public boolean visit(TextBlockLiteralExpr n)
	{
		return visitTextBlockLiteralExpr(n);

	}

	@Override
	public boolean visit(YieldStmt yieldStmt)
	{
		return visitYieldStmt(yieldStmt);

	}

	@Override
	public boolean visit(TypePatternExpr n)
	{
		return visitTypePatternExpr(n);

	}

	@Override
	public boolean visit(RecordPatternExpr n)
	{
		return visitRecordPatternExpr(n);

	}

	// --- Added missing visitXXX and leaveXXX methods ---
	protected boolean leaveBinaryExpr(BinaryExpr n)
	{
		return true;
	}

	protected boolean leaveBlockComment(BlockComment n)
	{
		return true;
	}

	protected boolean leaveBlockStmt(BlockStmt n)
	{
		return true;
	}

	protected boolean leaveBooleanLiteralExpr(BooleanLiteralExpr n)
	{
		return true;
	}

	protected boolean leaveBreakStmt(BreakStmt n)
	{
		return true;
	}

	protected boolean leaveCastExpr(CastExpr n)
	{
		return true;
	}

	protected boolean leaveCatchClause(CatchClause n)
	{
		return true;
	}

	protected boolean leaveCharLiteralExpr(CharLiteralExpr n)
	{
		return true;
	}

	protected boolean leaveClassExpr(ClassExpr n)
	{
		return true;
	}

	protected boolean leaveClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration n)
	{
		return true;
	}

	protected boolean leaveClassOrInterfaceType(ClassOrInterfaceType n)
	{
		return true;
	}

	protected boolean leaveCompilationUnit(CompilationUnit n)
	{
		return true;
	}

	protected boolean leaveConditionalExpr(ConditionalExpr n)
	{
		return true;
	}

	protected boolean leaveConstructorDeclaration(ConstructorDeclaration n)
	{
		return true;
	}

	protected boolean leaveContinueStmt(ContinueStmt n)
	{
		return true;
	}

	protected boolean leaveDoStmt(DoStmt n)
	{
		return true;
	}

	protected boolean leaveDoubleLiteralExpr(DoubleLiteralExpr n)
	{
		return true;
	}

	protected boolean leaveEmptyStmt(EmptyStmt n)
	{
		return true;
	}

	protected boolean leaveEnclosedExpr(EnclosedExpr n)
	{
		return true;
	}

	protected boolean leaveEnumConstantDeclaration(EnumConstantDeclaration n)
	{
		return true;
	}

	protected boolean leaveEnumDeclaration(EnumDeclaration n)
	{
		return true;
	}

	protected boolean leaveExplicitConstructorInvocationStmt(ExplicitConstructorInvocationStmt n)
	{
		return true;
	}

	protected boolean leaveExpressionStmt(ExpressionStmt n)
	{
		return true;
	}

	protected boolean leaveFieldAccessExpr(FieldAccessExpr n)
	{
		return true;
	}

	protected boolean leaveFieldDeclaration(FieldDeclaration n)
	{
		return true;
	}

	protected boolean leaveForStmt(ForStmt n)
	{
		return true;
	}

	protected boolean leaveForEachStmt(ForEachStmt n)
	{
		return true;
	}

	protected boolean leaveIfStmt(IfStmt n)
	{
		return true;
	}

	protected boolean leaveImportDeclaration(ImportDeclaration n)
	{
		return true;
	}

	protected boolean leaveInitializerDeclaration(InitializerDeclaration n)
	{
		return true;
	}

	protected boolean leaveInstanceOfExpr(InstanceOfExpr n)
	{
		return true;
	}

	protected boolean leaveIntegerLiteralExpr(IntegerLiteralExpr n)
	{
		return true;
	}

	protected boolean leaveIntersectionType(IntersectionType n)
	{
		return true;
	}

	protected boolean leaveJavadocComment(JavadocComment n)
	{
		return true;
	}

	protected boolean leaveLabeledStmt(LabeledStmt n)
	{
		return true;
	}

	protected boolean leaveLambdaExpr(LambdaExpr n)
	{
		return true;
	}

	protected boolean leaveLineComment(LineComment n)
	{
		return true;
	}

	protected boolean leaveLocalClassDeclarationStmt(LocalClassDeclarationStmt n)
	{
		return true;
	}

	protected boolean leaveLocalRecordDeclarationStmt(LocalRecordDeclarationStmt n)
	{
		return true;
	}

	protected boolean leaveLongLiteralExpr(LongLiteralExpr n)
	{
		return true;
	}

	protected boolean leaveMarkerAnnotationExpr(MarkerAnnotationExpr n)
	{
		return true;
	}

	protected boolean leaveMemberValuePair(MemberValuePair n)
	{
		return true;
	}

	protected boolean leaveMethodCallExpr(MethodCallExpr n)
	{
		return true;
	}

	protected boolean leaveMethodDeclaration(MethodDeclaration n)
	{
		return true;
	}

	protected boolean leaveMethodReferenceExpr(MethodReferenceExpr n)
	{
		return true;
	}

	protected boolean leaveNameExpr(NameExpr n)
	{
		return true;
	}

	protected boolean leaveName(Name n)
	{
		return true;
	}

	protected boolean leaveNormalAnnotationExpr(NormalAnnotationExpr n)
	{
		return true;
	}

	protected boolean leaveNullLiteralExpr(NullLiteralExpr n)
	{
		return true;
	}

	protected boolean leaveObjectCreationExpr(ObjectCreationExpr n)
	{
		return true;
	}

	protected boolean leavePackageDeclaration(PackageDeclaration n)
	{
		return true;
	}

	protected boolean leaveParameter(Parameter n)
	{
		return true;
	}

	protected boolean leavePrimitiveType(PrimitiveType n)
	{
		return true;
	}

	protected boolean leaveRecordDeclaration(RecordDeclaration n)
	{
		return true;
	}

	protected boolean leaveCompactConstructorDeclaration(CompactConstructorDeclaration n)
	{
		return true;
	}

	protected boolean leaveReturnStmt(ReturnStmt n)
	{
		return true;
	}

	protected boolean leaveSimpleName(SimpleName n)
	{
		return true;
	}

	protected boolean leaveSingleMemberAnnotationExpr(SingleMemberAnnotationExpr n)
	{
		return true;
	}

	protected boolean leaveStringLiteralExpr(StringLiteralExpr n)
	{
		return true;
	}

	protected boolean leaveSuperExpr(SuperExpr n)
	{
		return true;
	}

	protected boolean leaveSwitchEntry(SwitchEntry n)
	{
		return true;
	}

	protected boolean leaveSwitchStmt(SwitchStmt n)
	{
		return true;
	}

	protected boolean leaveSynchronizedStmt(SynchronizedStmt n)
	{
		return true;
	}

	protected boolean leaveThisExpr(ThisExpr n)
	{
		return true;
	}

	protected boolean leaveThrowStmt(ThrowStmt n)
	{
		return true;
	}

	protected boolean leaveTryStmt(TryStmt n)
	{
		return true;
	}

	protected boolean leaveTypeExpr(TypeExpr n)
	{
		return true;
	}

	protected boolean leaveTypeParameter(TypeParameter n)
	{
		return true;
	}

	protected boolean leaveUnaryExpr(UnaryExpr n)
	{
		return true;
	}

	protected boolean leaveUnionType(UnionType n)
	{
		return true;
	}

	protected boolean leaveUnknownType(UnknownType n)
	{
		return true;
	}

	protected boolean leaveVariableDeclarationExpr(VariableDeclarationExpr n)
	{
		return true;
	}

	protected boolean leaveVariableDeclarator(VariableDeclarator n)
	{
		return true;
	}

	protected boolean leaveVoidType(VoidType n)
	{
		return true;
	}

	protected boolean leaveWhileStmt(WhileStmt n)
	{
		return true;
	}

	protected boolean leaveWildcardType(WildcardType n)
	{
		return true;
	}

	protected boolean leaveModuleDeclaration(ModuleDeclaration n)
	{
		return true;
	}

	protected boolean leaveModuleRequiresDirective(ModuleRequiresDirective n)
	{
		return true;
	}

	protected boolean leaveModuleExportsDirective(ModuleExportsDirective n)
	{
		return true;
	}

	protected boolean leaveModuleProvidesDirective(ModuleProvidesDirective n)
	{
		return true;
	}

	protected boolean leaveModuleUsesDirective(ModuleUsesDirective n)
	{
		return true;
	}

	protected boolean leaveModuleOpensDirective(ModuleOpensDirective n)
	{
		return true;
	}

	protected boolean leaveUnparsableStmt(UnparsableStmt n)
	{
		return true;
	}

	protected boolean leaveReceiverParameter(ReceiverParameter n)
	{
		return true;
	}

	protected boolean leaveVarType(VarType n)
	{
		return true;
	}

	protected boolean leaveModifier(Modifier n)
	{
		return true;
	}

	protected boolean leaveSwitchExpr(SwitchExpr n)
	{
		return true;
	}

	protected boolean leaveTextBlockLiteralExpr(TextBlockLiteralExpr n)
	{
		return true;
	}

	protected boolean leaveYieldStmt(YieldStmt n)
	{
		return true;
	}

	protected boolean leaveTypePatternExpr(TypePatternExpr n)
	{
		return true;
	}

	protected boolean leaveRecordPatternExpr(RecordPatternExpr n)
	{
		return true;
	}

	protected boolean visitNodeList(@SuppressWarnings("rawtypes") NodeList n)
	{
		return true;
	}

	protected boolean visitAnnotationDeclaration(AnnotationDeclaration n)
	{
		return true;
	}

	protected boolean visitAnnotationMemberDeclaration(AnnotationMemberDeclaration n)
	{
		return true;
	}

	protected boolean visitArrayAccessExpr(ArrayAccessExpr n)
	{
		return true;
	}

	protected boolean visitArrayCreationExpr(ArrayCreationExpr n)
	{
		return true;
	}

	protected boolean visitArrayCreationLevel(ArrayCreationLevel n)
	{
		return true;
	}

	protected boolean visitArrayInitializerExpr(ArrayInitializerExpr n)
	{
		return true;
	}

	protected boolean visitArrayType(ArrayType n)
	{
		return true;
	}

	protected boolean visitAssertStmt(AssertStmt n)
	{
		return true;
	}

	protected boolean visitAssignExpr(AssignExpr n)
	{
		return true;
	}

	protected boolean visitBinaryExpr(BinaryExpr n)
	{
		return true;
	}

	protected boolean visitBlockComment(BlockComment n)
	{
		return true;
	}

	protected boolean visitBlockStmt(BlockStmt n)
	{
		return true;
	}

	protected boolean visitBooleanLiteralExpr(BooleanLiteralExpr n)
	{
		return true;
	}

	protected boolean visitBreakStmt(BreakStmt n)
	{
		return true;
	}

	protected boolean visitCastExpr(CastExpr n)
	{
		return true;
	}

	protected boolean visitCatchClause(CatchClause n)
	{
		return true;
	}

	protected boolean visitCharLiteralExpr(CharLiteralExpr n)
	{
		return true;
	}

	protected boolean visitClassExpr(ClassExpr n)
	{
		return true;
	}

	protected boolean visitClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration n)
	{
		return true;
	}

	protected boolean visitClassOrInterfaceType(ClassOrInterfaceType n)
	{
		return true;
	}

	protected boolean visitCompilationUnit(CompilationUnit n)
	{
		return true;
	}

	protected boolean visitConditionalExpr(ConditionalExpr n)
	{
		return true;
	}

	protected boolean visitConstructorDeclaration(ConstructorDeclaration n)
	{
		return true;
	}

	protected boolean visitContinueStmt(ContinueStmt n)
	{
		return true;
	}

	protected boolean visitDoStmt(DoStmt n)
	{
		return true;
	}

	protected boolean visitDoubleLiteralExpr(DoubleLiteralExpr n)
	{
		return true;
	}

	protected boolean visitEmptyStmt(EmptyStmt n)
	{
		return true;
	}

	protected boolean visitEnclosedExpr(EnclosedExpr n)
	{
		return true;
	}

	protected boolean visitEnumConstantDeclaration(EnumConstantDeclaration n)
	{
		return true;
	}

	protected boolean visitEnumDeclaration(EnumDeclaration n)
	{
		return true;
	}

	protected boolean visitExplicitConstructorInvocationStmt(ExplicitConstructorInvocationStmt n)
	{
		return true;
	}

	protected boolean visitExpressionStmt(ExpressionStmt n)
	{
		return true;
	}

	protected boolean visitFieldAccessExpr(FieldAccessExpr n)
	{
		return true;
	}

	protected boolean visitFieldDeclaration(FieldDeclaration n)
	{
		return true;
	}

	protected boolean visitForStmt(ForStmt n)
	{
		return true;
	}

	protected boolean visitForEachStmt(ForEachStmt n)
	{
		return true;
	}

	protected boolean visitIfStmt(IfStmt n)
	{
		return true;
	}

	protected boolean visitImportDeclaration(ImportDeclaration n)
	{
		return true;
	}

	protected boolean visitInitializerDeclaration(InitializerDeclaration n)
	{
		return true;
	}

	protected boolean visitInstanceOfExpr(InstanceOfExpr n)
	{
		return true;
	}

	protected boolean visitIntegerLiteralExpr(IntegerLiteralExpr n)
	{
		return true;
	}

	protected boolean visitIntersectionType(IntersectionType n)
	{
		return true;
	}

	protected boolean visitJavadocComment(JavadocComment n)
	{
		return true;
	}

	protected boolean visitLabeledStmt(LabeledStmt n)
	{
		return true;
	}

	protected boolean visitLambdaExpr(LambdaExpr n)
	{
		return true;
	}

	protected boolean visitLineComment(LineComment n)
	{
		return true;
	}

	protected boolean visitLocalClassDeclarationStmt(LocalClassDeclarationStmt n)
	{
		return true;
	}

	protected boolean visitLocalRecordDeclarationStmt(LocalRecordDeclarationStmt n)
	{
		return true;
	}

	protected boolean visitLongLiteralExpr(LongLiteralExpr n)
	{
		return true;
	}

	protected boolean visitMarkerAnnotationExpr(MarkerAnnotationExpr n)
	{
		return true;
	}

	protected boolean visitMemberValuePair(MemberValuePair n)
	{
		return true;
	}

	protected boolean visitMethodCallExpr(MethodCallExpr n)
	{
		System.out.println("ME");
		return true;
		//return true;
	}

	protected boolean visitMethodDeclaration(MethodDeclaration n)
	{
		return true;
	}

	protected boolean visitMethodReferenceExpr(MethodReferenceExpr n)
	{
		return true;
	}

	protected boolean visitNameExpr(NameExpr n)
	{
		return true;
	}

	protected boolean visitName(Name n)
	{
		return true;
	}

	protected boolean visitNormalAnnotationExpr(NormalAnnotationExpr n)
	{
		return true;
	}

	protected boolean visitNullLiteralExpr(NullLiteralExpr n)
	{
		return true;
	}

	protected boolean visitObjectCreationExpr(ObjectCreationExpr n)
	{
		return true;
	}

	protected boolean visitPackageDeclaration(PackageDeclaration n)
	{
		return true;
	}

	protected boolean visitParameter(Parameter n)
	{
		return true;
	}

	protected boolean visitPrimitiveType(PrimitiveType n)
	{
		return true;
	}

	protected boolean visitRecordDeclaration(RecordDeclaration n)
	{
		return true;
	}

	protected boolean visitCompactConstructorDeclaration(CompactConstructorDeclaration n)
	{
		return true;
	}

	protected boolean visitReturnStmt(ReturnStmt n)
	{
		return true;
	}

	protected boolean visitSimpleName(SimpleName n)
	{
		return true;
	}

	protected boolean visitSingleMemberAnnotationExpr(SingleMemberAnnotationExpr n)
	{
		return true;
	}

	protected boolean visitStringLiteralExpr(StringLiteralExpr n)
	{
		return true;
	}

	protected boolean visitSuperExpr(SuperExpr n)
	{
		return true;
	}

	protected boolean visitSwitchEntry(SwitchEntry n)
	{
		return true;
	}

	protected boolean visitSwitchStmt(SwitchStmt n)
	{
		return true;
	}

	protected boolean visitSynchronizedStmt(SynchronizedStmt n)
	{
		return true;
	}

	protected boolean visitThisExpr(ThisExpr n)
	{
		return true;
	}

	protected boolean visitThrowStmt(ThrowStmt n)
	{
		return true;
	}

	protected boolean visitTryStmt(TryStmt n)
	{
		return true;
	}

	protected boolean visitTypeExpr(TypeExpr n)
	{
		return true;
	}

	protected boolean visitTypeParameter(TypeParameter n)
	{
		return true;
	}

	protected boolean visitUnaryExpr(UnaryExpr n)
	{
		return true;
	}

	protected boolean visitUnionType(UnionType n)
	{
		return true;
	}

	protected boolean visitUnknownType(UnknownType n)
	{
		return true;
	}

	protected boolean visitVariableDeclarationExpr(VariableDeclarationExpr n)
	{
		return true;
	}

	protected boolean visitVariableDeclarator(VariableDeclarator n)
	{
		return true;
	}

	protected boolean visitVoidType(VoidType n)
	{
		return true;
	}

	protected boolean visitWhileStmt(WhileStmt n)
	{
		return true;
	}

	protected boolean visitWildcardType(WildcardType n)
	{
		return true;
	}

	protected boolean visitModuleDeclaration(ModuleDeclaration n)
	{
		return true;
	}

	protected boolean visitModuleRequiresDirective(ModuleRequiresDirective n)
	{
		return true;
	}

	protected boolean visitModuleExportsDirective(ModuleExportsDirective n)
	{
		return true;
	}

	protected boolean visitModuleProvidesDirective(ModuleProvidesDirective n)
	{
		return true;
	}

	protected boolean visitModuleUsesDirective(ModuleUsesDirective n)
	{
		return true;
	}

	protected boolean visitModuleOpensDirective(ModuleOpensDirective n)
	{
		return true;
	}

	protected boolean visitUnparsableStmt(UnparsableStmt n)
	{
		return true;
	}

	protected boolean visitReceiverParameter(ReceiverParameter n)
	{
		return true;
	}

	protected boolean visitVarType(VarType n)
	{
		return true;
	}

	protected boolean visitModifier(Modifier n)
	{
		return true;
	}

	protected boolean visitSwitchExpr(SwitchExpr n)
	{
		return true;
	}

	protected boolean visitTextBlockLiteralExpr(TextBlockLiteralExpr n)
	{
		return true;
	}

	protected boolean visitYieldStmt(YieldStmt n)
	{
		return true;
	}

	protected boolean visitTypePatternExpr(TypePatternExpr n)
	{
		return true;
	}

	protected boolean visitRecordPatternExpr(RecordPatternExpr n) { return true; }
}