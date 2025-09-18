package ca.uqac.lif.codefinder.find.visitor;

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
	public void leave(NodeList n); 

  public void leave(AnnotationDeclaration n); 

  public void leave(AnnotationMemberDeclaration n); 

  public void leave(ArrayAccessExpr n); 

  public void leave(ArrayCreationExpr n); 

  public void leave(ArrayCreationLevel n); 

  public void leave(ArrayInitializerExpr n); 

  public void leave(ArrayType n); 

  public void leave(AssertStmt n); 

  public void leave(AssignExpr n); 

  public void leave(BinaryExpr n); 

  public void leave(BlockComment n); 

  public void leave(BlockStmt n); 

  public void leave(BooleanLiteralExpr n); 

  public void leave(BreakStmt n); 

  public void leave(CastExpr n); 

  public void leave(CatchClause n); 

  public void leave(CharLiteralExpr n); 

  public void leave(ClassExpr n); 

  public void leave(ClassOrInterfaceDeclaration n); 

  public void leave(ClassOrInterfaceType n); 

  public void leave(CompilationUnit n); 

  public void leave(ConditionalExpr n); 

  public void leave(ConstructorDeclaration n); 

  public void leave(ContinueStmt n); 

  public void leave(DoStmt n); 

  public void leave(DoubleLiteralExpr n); 

  public void leave(EmptyStmt n); 

  public void leave(EnclosedExpr n); 

  public void leave(EnumConstantDeclaration n); 

  public void leave(EnumDeclaration n); 

  public void leave(ExplicitConstructorInvocationStmt n); 

  public void leave(ExpressionStmt n); 

  public void leave(FieldAccessExpr n); 

  public void leave(FieldDeclaration n); 

  public void leave(ForStmt n); 

  public void leave(ForEachStmt n); 

  public void leave(IfStmt n); 

  public void leave(ImportDeclaration n); 

  public void leave(InitializerDeclaration n); 

  public void leave(InstanceOfExpr n); 

  public void leave(IntegerLiteralExpr n); 

  public void leave(IntersectionType n); 

  public void leave(JavadocComment n); 

  public void leave(LabeledStmt n); 

  public void leave(LambdaExpr n); 

  public void leave(LineComment n); 

  public void leave(LocalClassDeclarationStmt n); 

  public void leave(LocalRecordDeclarationStmt n); 

  public void leave(LongLiteralExpr n); 

  public void leave(MarkerAnnotationExpr n); 

  public void leave(MemberValuePair n); 

  public void leave(MethodCallExpr n); 

  public void leave(MethodDeclaration n); 

  public void leave(MethodReferenceExpr n); 

  public void leave(NameExpr n); 

  public void leave(Name n); 

  public void leave(NormalAnnotationExpr n); 

  public void leave(NullLiteralExpr n); 

  public void leave(ObjectCreationExpr n); 

  public void leave(PackageDeclaration n); 

  public void leave(Parameter n); 

  public void leave(PrimitiveType n); 

  public void leave(RecordDeclaration n); 

  public void leave(CompactConstructorDeclaration n); 

  public void leave(ReturnStmt n); 

  public void leave(SimpleName n); 

  public void leave(SingleMemberAnnotationExpr n); 

  public void leave(StringLiteralExpr n); 

  public void leave(SuperExpr n); 

  public void leave(SwitchEntry n); 

  public void leave(SwitchStmt n); 

  public void leave(SynchronizedStmt n); 

  public void leave(ThisExpr n); 

  public void leave(ThrowStmt n); 

  public void leave(TryStmt n); 

  public void leave(TypeExpr n); 

  public void leave(TypeParameter n); 

  public void leave(UnaryExpr n); 

  public void leave(UnionType n); 

  public void leave(UnknownType n); 

  public void leave(VariableDeclarationExpr n); 

  public void leave(VariableDeclarator n); 

  public void leave(VoidType n); 

  public void leave(WhileStmt n); 

  public void leave(WildcardType n); 

  public void leave(ModuleDeclaration n); 

  public void leave(ModuleRequiresDirective n); 

  public void leave(ModuleExportsDirective n); 

  public void leave(ModuleProvidesDirective n); 

  public void leave(ModuleUsesDirective n); 

  public void leave(ModuleOpensDirective n); 

  public void leave(UnparsableStmt n); 

  public void leave(ReceiverParameter n); 

  public void leave(VarType n); 

  public void leave(Modifier n); 

  public void leave(SwitchExpr switchExpr);

  public void leave(TextBlockLiteralExpr n); 

  public void leave(YieldStmt yieldStmt);

  public void leave(TypePatternExpr n); 

  public void leave(RecordPatternExpr n); 
  
  @SuppressWarnings("rawtypes")
	public void visit(NodeList n); 

  public void visit(AnnotationDeclaration n); 

  public void visit(AnnotationMemberDeclaration n); 

  public void visit(ArrayAccessExpr n); 

  public void visit(ArrayCreationExpr n); 

  public void visit(ArrayCreationLevel n); 

  public void visit(ArrayInitializerExpr n); 

  public void visit(ArrayType n); 

  public void visit(AssertStmt n); 

  public void visit(AssignExpr n); 

  public void visit(BinaryExpr n); 

  public void visit(BlockComment n); 

  public void visit(BlockStmt n); 

  public void visit(BooleanLiteralExpr n); 

  public void visit(BreakStmt n); 

  public void visit(CastExpr n); 

  public void visit(CatchClause n); 

  public void visit(CharLiteralExpr n); 

  public void visit(ClassExpr n); 

  public void visit(ClassOrInterfaceDeclaration n); 

  public void visit(ClassOrInterfaceType n); 

  public void visit(CompilationUnit n); 

  public void visit(ConditionalExpr n); 

  public void visit(ConstructorDeclaration n); 

  public void visit(ContinueStmt n); 

  public void visit(DoStmt n); 

  public void visit(DoubleLiteralExpr n); 

  public void visit(EmptyStmt n); 

  public void visit(EnclosedExpr n); 

  public void visit(EnumConstantDeclaration n); 

  public void visit(EnumDeclaration n); 

  public void visit(ExplicitConstructorInvocationStmt n); 

  public void visit(ExpressionStmt n); 

  public void visit(FieldAccessExpr n); 

  public void visit(FieldDeclaration n); 

  public void visit(ForStmt n); 

  public void visit(ForEachStmt n); 

  public void visit(IfStmt n); 

  public void visit(ImportDeclaration n); 

  public void visit(InitializerDeclaration n); 

  public void visit(InstanceOfExpr n); 

  public void visit(IntegerLiteralExpr n); 

  public void visit(IntersectionType n); 

  public void visit(JavadocComment n); 

  public void visit(LabeledStmt n); 

  public void visit(LambdaExpr n); 

  public void visit(LineComment n); 

  public void visit(LocalClassDeclarationStmt n); 

  public void visit(LocalRecordDeclarationStmt n); 

  public void visit(LongLiteralExpr n); 

  public void visit(MarkerAnnotationExpr n); 

  public void visit(MemberValuePair n); 

  public void visit(MethodCallExpr n); 

  public void visit(MethodDeclaration n); 

  public void visit(MethodReferenceExpr n); 

  public void visit(NameExpr n); 

  public void visit(Name n); 

  public void visit(NormalAnnotationExpr n); 

  public void visit(NullLiteralExpr n); 

  public void visit(ObjectCreationExpr n); 

  public void visit(PackageDeclaration n); 

  public void visit(Parameter n); 

  public void visit(PrimitiveType n); 

  public void visit(RecordDeclaration n); 

  public void visit(CompactConstructorDeclaration n); 

  public void visit(ReturnStmt n); 

  public void visit(SimpleName n); 

  public void visit(SingleMemberAnnotationExpr n); 

  public void visit(StringLiteralExpr n); 

  public void visit(SuperExpr n); 

  public void visit(SwitchEntry n); 

  public void visit(SwitchStmt n); 

  public void visit(SynchronizedStmt n); 

  public void visit(ThisExpr n); 

  public void visit(ThrowStmt n); 

  public void visit(TryStmt n); 

  public void visit(TypeExpr n); 

  public void visit(TypeParameter n); 

  public void visit(UnaryExpr n); 

  public void visit(UnionType n); 

  public void visit(UnknownType n); 

  public void visit(VariableDeclarationExpr n); 

  public void visit(VariableDeclarator n); 

  public void visit(VoidType n); 

  public void visit(WhileStmt n); 

  public void visit(WildcardType n); 

  public void visit(ModuleDeclaration n); 

  public void visit(ModuleRequiresDirective n); 

  public void visit(ModuleExportsDirective n); 

  public void visit(ModuleProvidesDirective n); 

  public void visit(ModuleUsesDirective n); 

  public void visit(ModuleOpensDirective n); 

  public void visit(UnparsableStmt n); 

  public void visit(ReceiverParameter n); 

  public void visit(VarType n); 

  public void visit(Modifier n); 

  public void visit(SwitchExpr switchExpr);

  public void visit(TextBlockLiteralExpr n); 

  public void visit(YieldStmt yieldStmt); 

  public void visit(TypePatternExpr n); 

  public void visit(RecordPatternExpr n);
  
  /**
   * Indicates whether the visiting should stop. This method is typically
   * called at the beginning of each visit/leave method to determine whether
   * the method should proceed or return immediately.
   * @return true if the visiting should stop, false otherwise
   */
  public boolean shouldStop();
  
  /**
	 * Stops the visiting of the AST by throwing a {@link StopVisitingException}.
	 * @throws StopVisitingException Always thrown to stop the visiting
	 */
  public void stop();
  
  /**
   * Resets the internal state of the visitor, if any.
   */
  public void reset();
}