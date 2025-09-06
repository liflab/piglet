package ca.uqac.lif.codefinder.find.ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.modules.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;

public interface PushPopVisitor
{
  @SuppressWarnings("rawtypes")
	public boolean leave(NodeList n); 

  public boolean leave(AnnotationDeclaration n); 

  public boolean leave(AnnotationMemberDeclaration n); 

  public boolean leave(ArrayAccessExpr n); 

  public boolean leave(ArrayCreationExpr n); 

  public boolean leave(ArrayCreationLevel n); 

  public boolean leave(ArrayInitializerExpr n); 

  public boolean leave(ArrayType n); 

  public boolean leave(AssertStmt n); 

  public boolean leave(AssignExpr n); 

  public boolean leave(BinaryExpr n); 

  public boolean leave(BlockComment n); 

  public boolean leave(BlockStmt n); 

  public boolean leave(BooleanLiteralExpr n); 

  public boolean leave(BreakStmt n); 

  public boolean leave(CastExpr n); 

  public boolean leave(CatchClause n); 

  public boolean leave(CharLiteralExpr n); 

  public boolean leave(ClassExpr n); 

  public boolean leave(ClassOrInterfaceDeclaration n); 

  public boolean leave(ClassOrInterfaceType n); 

  public boolean leave(CompilationUnit n); 

  public boolean leave(ConditionalExpr n); 

  public boolean leave(ConstructorDeclaration n); 

  public boolean leave(ContinueStmt n); 

  public boolean leave(DoStmt n); 

  public boolean leave(DoubleLiteralExpr n); 

  public boolean leave(EmptyStmt n); 

  public boolean leave(EnclosedExpr n); 

  public boolean leave(EnumConstantDeclaration n); 

  public boolean leave(EnumDeclaration n); 

  public boolean leave(ExplicitConstructorInvocationStmt n); 

  public boolean leave(ExpressionStmt n); 

  public boolean leave(FieldAccessExpr n); 

  public boolean leave(FieldDeclaration n); 

  public boolean leave(ForStmt n); 

  public boolean leave(ForEachStmt n); 

  public boolean leave(IfStmt n); 

  public boolean leave(ImportDeclaration n); 

  public boolean leave(InitializerDeclaration n); 

  public boolean leave(InstanceOfExpr n); 

  public boolean leave(IntegerLiteralExpr n); 

  public boolean leave(IntersectionType n); 

  public boolean leave(JavadocComment n); 

  public boolean leave(LabeledStmt n); 

  public boolean leave(LambdaExpr n); 

  public boolean leave(LineComment n); 

  public boolean leave(LocalClassDeclarationStmt n); 

  public boolean leave(LocalRecordDeclarationStmt n); 

  public boolean leave(LongLiteralExpr n); 

  public boolean leave(MarkerAnnotationExpr n); 

  public boolean leave(MemberValuePair n); 

  public boolean leave(MethodCallExpr n); 

  public boolean leave(MethodDeclaration n); 

  public boolean leave(MethodReferenceExpr n); 

  public boolean leave(NameExpr n); 

  public boolean leave(Name n); 

  public boolean leave(NormalAnnotationExpr n); 

  public boolean leave(NullLiteralExpr n); 

  public boolean leave(ObjectCreationExpr n); 

  public boolean leave(PackageDeclaration n); 

  public boolean leave(Parameter n); 

  public boolean leave(PrimitiveType n); 

  public boolean leave(RecordDeclaration n); 

  public boolean leave(CompactConstructorDeclaration n); 

  public boolean leave(ReturnStmt n); 

  public boolean leave(SimpleName n); 

  public boolean leave(SingleMemberAnnotationExpr n); 

  public boolean leave(StringLiteralExpr n); 

  public boolean leave(SuperExpr n); 

  public boolean leave(SwitchEntry n); 

  public boolean leave(SwitchStmt n); 

  public boolean leave(SynchronizedStmt n); 

  public boolean leave(ThisExpr n); 

  public boolean leave(ThrowStmt n); 

  public boolean leave(TryStmt n); 

  public boolean leave(TypeExpr n); 

  public boolean leave(TypeParameter n); 

  public boolean leave(UnaryExpr n); 

  public boolean leave(UnionType n); 

  public boolean leave(UnknownType n); 

  public boolean leave(VariableDeclarationExpr n); 

  public boolean leave(VariableDeclarator n); 

  public boolean leave(VoidType n); 

  public boolean leave(WhileStmt n); 

  public boolean leave(WildcardType n); 

  public boolean leave(ModuleDeclaration n); 

  public boolean leave(ModuleRequiresDirective n); 

  public boolean leave(ModuleExportsDirective n); 

  public boolean leave(ModuleProvidesDirective n); 

  public boolean leave(ModuleUsesDirective n); 

  public boolean leave(ModuleOpensDirective n); 

  public boolean leave(UnparsableStmt n); 

  public boolean leave(ReceiverParameter n); 

  public boolean leave(VarType n); 

  public boolean leave(Modifier n); 

  public boolean leave(SwitchExpr switchExpr);

  public boolean leave(TextBlockLiteralExpr n); 

  public boolean leave(YieldStmt yieldStmt);

  public boolean leave(TypePatternExpr n); 

  public boolean leave(RecordPatternExpr n); 
  
  @SuppressWarnings("rawtypes")
	public boolean visit(NodeList n); 

  public boolean visit(AnnotationDeclaration n); 

  public boolean visit(AnnotationMemberDeclaration n); 

  public boolean visit(ArrayAccessExpr n); 

  public boolean visit(ArrayCreationExpr n); 

  public boolean visit(ArrayCreationLevel n); 

  public boolean visit(ArrayInitializerExpr n); 

  public boolean visit(ArrayType n); 

  public boolean visit(AssertStmt n); 

  public boolean visit(AssignExpr n); 

  public boolean visit(BinaryExpr n); 

  public boolean visit(BlockComment n); 

  public boolean visit(BlockStmt n); 

  public boolean visit(BooleanLiteralExpr n); 

  public boolean visit(BreakStmt n); 

  public boolean visit(CastExpr n); 

  public boolean visit(CatchClause n); 

  public boolean visit(CharLiteralExpr n); 

  public boolean visit(ClassExpr n); 

  public boolean visit(ClassOrInterfaceDeclaration n); 

  public boolean visit(ClassOrInterfaceType n); 

  public boolean visit(CompilationUnit n); 

  public boolean visit(ConditionalExpr n); 

  public boolean visit(ConstructorDeclaration n); 

  public boolean visit(ContinueStmt n); 

  public boolean visit(DoStmt n); 

  public boolean visit(DoubleLiteralExpr n); 

  public boolean visit(EmptyStmt n); 

  public boolean visit(EnclosedExpr n); 

  public boolean visit(EnumConstantDeclaration n); 

  public boolean visit(EnumDeclaration n); 

  public boolean visit(ExplicitConstructorInvocationStmt n); 

  public boolean visit(ExpressionStmt n); 

  public boolean visit(FieldAccessExpr n); 

  public boolean visit(FieldDeclaration n); 

  public boolean visit(ForStmt n); 

  public boolean visit(ForEachStmt n); 

  public boolean visit(IfStmt n); 

  public boolean visit(ImportDeclaration n); 

  public boolean visit(InitializerDeclaration n); 

  public boolean visit(InstanceOfExpr n); 

  public boolean visit(IntegerLiteralExpr n); 

  public boolean visit(IntersectionType n); 

  public boolean visit(JavadocComment n); 

  public boolean visit(LabeledStmt n); 

  public boolean visit(LambdaExpr n); 

  public boolean visit(LineComment n); 

  public boolean visit(LocalClassDeclarationStmt n); 

  public boolean visit(LocalRecordDeclarationStmt n); 

  public boolean visit(LongLiteralExpr n); 

  public boolean visit(MarkerAnnotationExpr n); 

  public boolean visit(MemberValuePair n); 

  public boolean visit(MethodCallExpr n); 

  public boolean visit(MethodDeclaration n); 

  public boolean visit(MethodReferenceExpr n); 

  public boolean visit(NameExpr n); 

  public boolean visit(Name n); 

  public boolean visit(NormalAnnotationExpr n); 

  public boolean visit(NullLiteralExpr n); 

  public boolean visit(ObjectCreationExpr n); 

  public boolean visit(PackageDeclaration n); 

  public boolean visit(Parameter n); 

  public boolean visit(PrimitiveType n); 

  public boolean visit(RecordDeclaration n); 

  public boolean visit(CompactConstructorDeclaration n); 

  public boolean visit(ReturnStmt n); 

  public boolean visit(SimpleName n); 

  public boolean visit(SingleMemberAnnotationExpr n); 

  public boolean visit(StringLiteralExpr n); 

  public boolean visit(SuperExpr n); 

  public boolean visit(SwitchEntry n); 

  public boolean visit(SwitchStmt n); 

  public boolean visit(SynchronizedStmt n); 

  public boolean visit(ThisExpr n); 

  public boolean visit(ThrowStmt n); 

  public boolean visit(TryStmt n); 

  public boolean visit(TypeExpr n); 

  public boolean visit(TypeParameter n); 

  public boolean visit(UnaryExpr n); 

  public boolean visit(UnionType n); 

  public boolean visit(UnknownType n); 

  public boolean visit(VariableDeclarationExpr n); 

  public boolean visit(VariableDeclarator n); 

  public boolean visit(VoidType n); 

  public boolean visit(WhileStmt n); 

  public boolean visit(WildcardType n); 

  public boolean visit(ModuleDeclaration n); 

  public boolean visit(ModuleRequiresDirective n); 

  public boolean visit(ModuleExportsDirective n); 

  public boolean visit(ModuleProvidesDirective n); 

  public boolean visit(ModuleUsesDirective n); 

  public boolean visit(ModuleOpensDirective n); 

  public boolean visit(UnparsableStmt n); 

  public boolean visit(ReceiverParameter n); 

  public boolean visit(VarType n); 

  public boolean visit(Modifier n); 

  public boolean visit(SwitchExpr switchExpr);

  public boolean visit(TextBlockLiteralExpr n); 

  public boolean visit(YieldStmt yieldStmt); 

  public boolean visit(TypePatternExpr n); 

  public boolean visit(RecordPatternExpr n); 
}