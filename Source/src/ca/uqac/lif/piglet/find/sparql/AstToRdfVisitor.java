package ca.uqac.lif.piglet.find.sparql;

import java.util.Optional;
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
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;

import ca.uqac.lif.piglet.find.TokenFinderContext;
import ca.uqac.lif.piglet.find.visitor.PushPopVisitableNode;
import ca.uqac.lif.piglet.find.visitor.PushPopVisitor;

public abstract class AstToRdfVisitor implements PushPopVisitor
{
	public static final Property IN = ResourceFactory.createProperty(ModelBuilder.NS, "in");

	public static final Property NODETYPE = ResourceFactory.createProperty(ModelBuilder.NS,
			"nodetype");

	public static final Property JAVADOC = ResourceFactory.createProperty(ModelBuilder.NS, "javadoc");

	/** An index of AST nodes to RDF resources */
	protected final LazyNodeIndex<Node, String> m_index;

	/** The RDF model being built */
	protected final Model m_model;

	/** A stack of parent RDF resources */
	protected final Stack<Resource> m_parents = new Stack<>();

	/** The root of the AST in RDF */
	protected Resource m_root = null;

	/** Whether the visitor should stop */
	protected boolean m_shouldStop = false;

	/** How many levels of class declarations to follow */
	protected final int m_follow;

	/** The context in which the token finder operates */
	protected final TokenFinderContext m_context;

	/** The name of the file being processed */
	protected final String m_filename;

	/**
	 * Creates a new visitor.
	 */
	public AstToRdfVisitor(int follow, TokenFinderContext context, String filename)
	{
		super();
		m_follow = follow;
		m_index = new JavaAstNodeIndex();
		m_model = ModelFactory.createDefaultModel();
		m_context = context;
		m_filename = filename;
	}

	protected AstToRdfVisitor(Model m, LazyNodeIndex<Node, String> index, Resource parent, int follow,
			TokenFinderContext context, String filename)
	{
		super();
		m_context = context;
		m_follow = follow;
		m_index = index;
		m_model = m;
		if (parent != null)
		{
			m_parents.push(parent);
			m_root = parent;
		}
		m_filename = filename;
	}

	/**
	 * Creates a new visitor.
	 * 
	 * @param m
	 *          The RDF model to populate
	 * @param index
	 *          The index of AST nodes to RDF resources
	 * @param follow
	 *          How many levels of class declarations to follow
	 * @param context
	 *          The context in which the token finder operates
	 * @param filename
	 *          The name of the file being processed
	 */
	protected AstToRdfVisitor(Model m, LazyNodeIndex<Node, String> index, int follow,
			TokenFinderContext context, String filename)
	{
		this(m, index, null, follow, context, filename);
	}

	/**
	 * Gets the index of AST nodes to RDF resources.
	 * 
	 * @return The index
	 */
	public LazyNodeIndex<Node, String> getIndex()
	{
		return m_index;
	}

	/**
	 * Gets the RDF model being built.
	 * 
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
		// genericleave(n);

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
		// Do not create nodes for ExpressionStmt, they are just containers
		// genericleave(n);
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
		// genericleave(n);

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
		// genericvisit(n);

	}

	@Override
	public void visit(AnnotationDeclaration n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(AnnotationMemberDeclaration n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ArrayAccessExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ArrayCreationExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ArrayCreationLevel n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ArrayInitializerExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ArrayType n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(AssertStmt n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(AssignExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(BinaryExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(BlockComment n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(BlockStmt n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(BooleanLiteralExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(BreakStmt n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(CastExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(CatchClause n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(CharLiteralExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ClassExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ClassOrInterfaceDeclaration n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ClassOrInterfaceType n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(CompilationUnit n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ConditionalExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ConstructorDeclaration n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ContinueStmt n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(DoStmt n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(DoubleLiteralExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(EmptyStmt n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(EnclosedExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(EnumConstantDeclaration n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(EnumDeclaration n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ExplicitConstructorInvocationStmt n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ExpressionStmt n)
	{
		// Do not create nodes for ExpressionStmt, they are just containers
		// genericvisit(n);
	}

	@Override
	public void visit(FieldAccessExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(FieldDeclaration n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ForStmt n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ForEachStmt n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(IfStmt n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ImportDeclaration n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(InitializerDeclaration n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(InstanceOfExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(IntegerLiteralExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(IntersectionType n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(JavadocComment n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(LabeledStmt n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(LambdaExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(LineComment n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(LocalClassDeclarationStmt n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(LocalRecordDeclarationStmt n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(LongLiteralExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(MarkerAnnotationExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(MemberValuePair n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(MethodCallExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(MethodDeclaration n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(MethodReferenceExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(NameExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(Name n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(NormalAnnotationExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(NullLiteralExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ObjectCreationExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(PackageDeclaration n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(Parameter n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(PrimitiveType n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(RecordDeclaration n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(CompactConstructorDeclaration n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ReturnStmt n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(SimpleName n)
	{
		// By design, we ignore SimpleName nodes; those that are relevant
		// are handled in their parent nodes
		// genericvisit(n);
	}

	@Override
	public void visit(SingleMemberAnnotationExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(StringLiteralExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(SuperExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(SwitchEntry n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(SwitchStmt n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(SynchronizedStmt n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ThisExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ThrowStmt n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(TryStmt n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(TypeExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(TypeParameter n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(UnaryExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(UnionType n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(UnknownType n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(VariableDeclarationExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(VariableDeclarator n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(VoidType n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(WhileStmt n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(WildcardType n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ModuleDeclaration n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ModuleRequiresDirective n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ModuleExportsDirective n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ModuleProvidesDirective n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ModuleUsesDirective n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ModuleOpensDirective n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(UnparsableStmt n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(ReceiverParameter n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(VarType n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(Modifier n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(SwitchExpr switchExpr)
	{
		if (!genericVisit(switchExpr))
			m_shouldStop = true;
	}

	@Override
	public void visit(TextBlockLiteralExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(YieldStmt yieldStmt)
	{
		if (!genericVisit(yieldStmt))
			m_shouldStop = true;
	}

	@Override
	public void visit(TypePatternExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	@Override
	public void visit(RecordPatternExpr n)
	{
		if (!genericVisit(n))
			m_shouldStop = true;
	}

	protected boolean genericVisit(Node n)
	{
		String iri = AstIds.iriFor(m_filename, n);
		if (m_index.containsIri(iri))
		{
			// Node already visited
			return false;
		}
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
		return true;
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

	/**
	 * Attempts to find the source file in which a method call expression is declared.
	 * @param callExpr The method call expression
	 * @return An optional containing the file name if found, or an empty optional
	 * otherwise
	 */
	protected static Optional<String> getDeclaringFileName(MethodCallExpr callExpr)
	{
		try
		{
			ResolvedMethodDeclaration rmd = callExpr.resolve();
			if (rmd instanceof JavaParserMethodDeclaration)
			{
				JavaParserMethodDeclaration jpmd = (JavaParserMethodDeclaration) rmd;
				MethodDeclaration md = jpmd.getWrappedNode();
				return md.findCompilationUnit().flatMap(CompilationUnit::getStorage)
						.map(storage -> storage.getPath().toString());
			}
			else
			{
				// method comes from JDK / library; no source file available
				return Optional.empty();
			}
		}
		catch (Exception e)
		{
			// resolution failed
			return Optional.empty();
		}
	}
}
