package ca.uqac.lif.codefinder.find.sparql;

import java.util.Stack;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
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

import ca.uqac.lif.codefinder.find.ast.PushPopVisitableNode;
import ca.uqac.lif.codefinder.find.ast.PushPopVisitor;

public abstract class AstToRdfVisitor implements PushPopVisitor
{
	public static final Property IN = ResourceFactory.createProperty(ModelBuilder.NS, "in");

	public static final Property NODETYPE = ResourceFactory.createProperty(ModelBuilder.NS, "nodetype");

	public static final Property NAME = ResourceFactory.createProperty(ModelBuilder.NS, "name");

	public static final Property ARGUMENTS = ResourceFactory.createProperty(ModelBuilder.NS, "args");

	public static final Property VALUE = ResourceFactory.createProperty(ModelBuilder.NS, "value");

	public static final Property RETURNS = ResourceFactory.createProperty(ModelBuilder.NS, "returns");

	/** An index of AST nodes to RDF resources */
	protected final JavaAstNodeIndex m_index;

	/** The RDF model being built */
	protected final Model m_model;

	protected final Stack<Resource> m_parents = new Stack<>();

	protected Resource m_root = null;
	
	protected boolean m_shouldStop = false;

	/**
	 * Creates a new visitor.
	 */
	public AstToRdfVisitor()
	{
		super();
		m_index = new JavaAstNodeIndex();
		m_model = ModelFactory.createDefaultModel();
	}

	protected AstToRdfVisitor(Model m, JavaAstNodeIndex index, Resource parent)
	{
		super();
		m_index = index;
		m_model = m;
		if (parent != null)
		{
			m_parents.push(parent);
		}
	}

	protected AstToRdfVisitor(Model m, JavaAstNodeIndex index)
	{
		this(m, index, null);
	}

	/**
	 * Gets the index of AST nodes to RDF resources.
	 * @return The index
	 */
	public JavaAstNodeIndex getIndex()
	{
		return m_index;
	}

	/**
	 * Gets the RDF model being built.
	 * @return The RDF model
	 */
	public Model getModel()
	{
		return m_model;
	}

	@Override
	public void leave(@SuppressWarnings("rawtypes") NodeList n)
	{
		// Assume this does not happen
		//genericleave(n);
		
	}

	@Override
	public void leave(AnnotationDeclaration n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(AnnotationMemberDeclaration n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ArrayAccessExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ArrayCreationExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ArrayCreationLevel n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ArrayInitializerExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ArrayType n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(AssertStmt n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(AssignExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(BinaryExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(BlockComment n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(BlockStmt n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(BooleanLiteralExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(BreakStmt n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(CastExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(CatchClause n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(CharLiteralExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ClassExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ClassOrInterfaceDeclaration n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ClassOrInterfaceType n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(CompilationUnit n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ConditionalExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ConstructorDeclaration n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ContinueStmt n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(DoStmt n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(DoubleLiteralExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(EmptyStmt n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(EnclosedExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(EnumConstantDeclaration n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(EnumDeclaration n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ExplicitConstructorInvocationStmt n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ExpressionStmt n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(FieldAccessExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(FieldDeclaration n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ForStmt n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ForEachStmt n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(IfStmt n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ImportDeclaration n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(InitializerDeclaration n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(InstanceOfExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(IntegerLiteralExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(IntersectionType n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(JavadocComment n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(LabeledStmt n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(LambdaExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(LineComment n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(LocalClassDeclarationStmt n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(LocalRecordDeclarationStmt n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(LongLiteralExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(MarkerAnnotationExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(MemberValuePair n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(MethodCallExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(MethodDeclaration n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(MethodReferenceExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(NameExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(Name n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(NormalAnnotationExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(NullLiteralExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ObjectCreationExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(PackageDeclaration n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(Parameter n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(PrimitiveType n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(RecordDeclaration n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(CompactConstructorDeclaration n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ReturnStmt n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(SimpleName n)
	{
		// By design, we ignore SimpleName nodes; those that are relevant
		// are handled in their parent nodes
		//genericleave(n);
		
	}

	@Override
	public void leave(SingleMemberAnnotationExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(StringLiteralExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(SuperExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(SwitchEntry n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(SwitchStmt n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(SynchronizedStmt n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ThisExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ThrowStmt n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(TryStmt n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(TypeExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(TypeParameter n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(UnaryExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(UnionType n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(UnknownType n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(VariableDeclarationExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(VariableDeclarator n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(VoidType n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(WhileStmt n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(WildcardType n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ModuleDeclaration n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ModuleRequiresDirective n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ModuleExportsDirective n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ModuleProvidesDirective n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ModuleUsesDirective n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ModuleOpensDirective n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(UnparsableStmt n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(ReceiverParameter n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(VarType n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(Modifier n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(SwitchExpr switchExpr)
	{
		genericleave(switchExpr);
		
	}

	@Override
	public void leave(TextBlockLiteralExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(YieldStmt yieldStmt)
	{
		genericleave(yieldStmt);
		
	}

	@Override
	public void leave(TypePatternExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void leave(RecordPatternExpr n)
	{
		genericleave(n);
		
	}

	@Override
	public void visit(@SuppressWarnings("rawtypes") NodeList n)
	{
		// Assume this does not happen
		//genericvisit(n);
		
	}

	@Override
	public void visit(AnnotationDeclaration n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(AnnotationMemberDeclaration n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ArrayAccessExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ArrayCreationExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ArrayCreationLevel n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ArrayInitializerExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ArrayType n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(AssertStmt n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(AssignExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(BinaryExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(BlockComment n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(BlockStmt n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(BooleanLiteralExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(BreakStmt n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(CastExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(CatchClause n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(CharLiteralExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ClassExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ClassOrInterfaceDeclaration n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ClassOrInterfaceType n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(CompilationUnit n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ConditionalExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ConstructorDeclaration n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ContinueStmt n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(DoStmt n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(DoubleLiteralExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(EmptyStmt n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(EnclosedExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(EnumConstantDeclaration n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(EnumDeclaration n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ExplicitConstructorInvocationStmt n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ExpressionStmt n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(FieldAccessExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(FieldDeclaration n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ForStmt n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ForEachStmt n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(IfStmt n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ImportDeclaration n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(InitializerDeclaration n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(InstanceOfExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(IntegerLiteralExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(IntersectionType n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(JavadocComment n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(LabeledStmt n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(LambdaExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(LineComment n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(LocalClassDeclarationStmt n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(LocalRecordDeclarationStmt n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(LongLiteralExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(MarkerAnnotationExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(MemberValuePair n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(MethodCallExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(MethodDeclaration n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(MethodReferenceExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(NameExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(Name n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(NormalAnnotationExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(NullLiteralExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ObjectCreationExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(PackageDeclaration n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(Parameter n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(PrimitiveType n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(RecordDeclaration n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(CompactConstructorDeclaration n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ReturnStmt n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(SimpleName n)
	{
		// By design, we ignore SimpleName nodes; those that are relevant
		// are handled in their parent nodes
		//genericvisit(n);
		
	}

	@Override
	public void visit(SingleMemberAnnotationExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(StringLiteralExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(SuperExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(SwitchEntry n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(SwitchStmt n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(SynchronizedStmt n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ThisExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ThrowStmt n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(TryStmt n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(TypeExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(TypeParameter n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(UnaryExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(UnionType n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(UnknownType n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(VariableDeclarationExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(VariableDeclarator n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(VoidType n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(WhileStmt n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(WildcardType n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ModuleDeclaration n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ModuleRequiresDirective n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ModuleExportsDirective n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ModuleProvidesDirective n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ModuleUsesDirective n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ModuleOpensDirective n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(UnparsableStmt n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(ReceiverParameter n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(VarType n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(Modifier n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(SwitchExpr switchExpr)
	{
		genericvisit(switchExpr);
		
	}

	@Override
	public void visit(TextBlockLiteralExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(YieldStmt yieldStmt)
	{
		genericvisit(yieldStmt);
		
	}

	@Override
	public void visit(TypePatternExpr n)
	{
		genericvisit(n);
		
	}

	@Override
	public void visit(RecordPatternExpr n)
	{
		genericvisit(n);
	}

	protected void genericvisit(Node n)
	{
		String iri = AstIds.iriFor("", n);
		Resource rdf_node = m_model.createResource(iri);
		Resource rdf_parent = m_parents.isEmpty() ? null : m_parents.peek();
		// If this is the first node, set it as root
		if (m_root == null)
		{
			m_root = rdf_parent != null ? rdf_parent : rdf_node;
		}
		m_parents.push(rdf_node);
		m_index.put(iri, n);
		if (rdf_parent != null)
		{
			m_model.add(rdf_parent, IN, rdf_node);
		}
		{
			Literal namenode = m_model.createLiteral(n.getClass().getSimpleName());
			m_model.add(rdf_node, NODETYPE, namenode);
		}
	}

	protected void genericleave(Node n)
	{
		m_parents.pop();
	}

	public Resource getRoot()
	{
		return m_root;
	}

	public void visit(PushPopVisitableNode n)
	{
	}

	public void leave(PushPopVisitableNode n)
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
	
	@Override
	public void reset()
	{
		m_shouldStop = false;
	}
}
