package ca.uqac.lif.codefinder.sparql;

import java.util.List;
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

import ca.uqac.lif.codefinder.ast.PushPopVisitableNode;
import ca.uqac.lif.codefinder.ast.PushPopVisitor;

public class AstToRdfVisitor implements PushPopVisitor
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
	public boolean leave(@SuppressWarnings("rawtypes") NodeList n)
	{
		// Assume this does not happen
		//genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(AnnotationDeclaration n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(AnnotationMemberDeclaration n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ArrayAccessExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ArrayCreationExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ArrayCreationLevel n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ArrayInitializerExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ArrayType n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(AssertStmt n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(AssignExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(BinaryExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(BlockComment n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(BlockStmt n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(BooleanLiteralExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(BreakStmt n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(CastExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(CatchClause n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(CharLiteralExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ClassExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ClassOrInterfaceDeclaration n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ClassOrInterfaceType n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(CompilationUnit n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ConditionalExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ConstructorDeclaration n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ContinueStmt n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(DoStmt n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(DoubleLiteralExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(EmptyStmt n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(EnclosedExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(EnumConstantDeclaration n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(EnumDeclaration n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ExplicitConstructorInvocationStmt n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ExpressionStmt n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(FieldAccessExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(FieldDeclaration n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ForStmt n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ForEachStmt n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(IfStmt n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ImportDeclaration n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(InitializerDeclaration n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(InstanceOfExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(IntegerLiteralExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(IntersectionType n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(JavadocComment n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(LabeledStmt n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(LambdaExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(LineComment n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(LocalClassDeclarationStmt n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(LocalRecordDeclarationStmt n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(LongLiteralExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(MarkerAnnotationExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(MemberValuePair n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(MethodCallExpr n)
	{
		genericLeave(n);
		m_parents.pop(); // Since we added the "args" node in visit()
		return true;
	}

	@Override
	public boolean leave(MethodDeclaration n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(MethodReferenceExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(NameExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(Name n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(NormalAnnotationExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(NullLiteralExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ObjectCreationExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(PackageDeclaration n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(Parameter n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(PrimitiveType n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(RecordDeclaration n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(CompactConstructorDeclaration n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ReturnStmt n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(SimpleName n)
	{
		// By design, we ignore SimpleName nodes; those that are relevant
		// are handled in their parent nodes
		//genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(SingleMemberAnnotationExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(StringLiteralExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(SuperExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(SwitchEntry n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(SwitchStmt n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(SynchronizedStmt n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ThisExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ThrowStmt n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(TryStmt n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(TypeExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(TypeParameter n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(UnaryExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(UnionType n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(UnknownType n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(VariableDeclarationExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(VariableDeclarator n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(VoidType n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(WhileStmt n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(WildcardType n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ModuleDeclaration n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ModuleRequiresDirective n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ModuleExportsDirective n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ModuleProvidesDirective n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ModuleUsesDirective n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ModuleOpensDirective n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(UnparsableStmt n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(ReceiverParameter n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(VarType n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(Modifier n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(SwitchExpr switchExpr)
	{
		genericLeave(switchExpr);
		return true;
	}

	@Override
	public boolean leave(TextBlockLiteralExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(YieldStmt yieldStmt)
	{
		genericLeave(yieldStmt);
		return true;
	}

	@Override
	public boolean leave(TypePatternExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean leave(RecordPatternExpr n)
	{
		genericLeave(n);
		return true;
	}

	@Override
	public boolean visit(@SuppressWarnings("rawtypes") NodeList n)
	{
		// Assume this does not happen
		//genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(AnnotationDeclaration n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(AnnotationMemberDeclaration n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ArrayAccessExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ArrayCreationExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ArrayCreationLevel n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ArrayInitializerExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ArrayType n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(AssertStmt n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(AssignExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(BinaryExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(BlockComment n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(BlockStmt n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(BooleanLiteralExpr n)
	{
		genericVisit(n);
		Resource bool_node = m_parents.peek();
		Literal name_node = m_model.createLiteral(Boolean.toString(n.getValue()));
		m_model.add(bool_node, VALUE, name_node);
		return false;
	}

	@Override
	public boolean visit(BreakStmt n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(CastExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(CatchClause n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(CharLiteralExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ClassExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ClassOrInterfaceDeclaration n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ClassOrInterfaceType n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(CompilationUnit n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ConditionalExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ConstructorDeclaration n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ContinueStmt n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(DoStmt n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(DoubleLiteralExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(EmptyStmt n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(EnclosedExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(EnumConstantDeclaration n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(EnumDeclaration n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ExplicitConstructorInvocationStmt n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ExpressionStmt n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(FieldAccessExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(FieldDeclaration n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ForStmt n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ForEachStmt n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(IfStmt n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ImportDeclaration n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(InitializerDeclaration n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(InstanceOfExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(IntegerLiteralExpr n)
	{
		genericVisit(n);
		Resource int_node = m_parents.peek();
		Literal name_node = m_model.createLiteral(n.getValue());
		m_model.add(int_node, VALUE, name_node);
		return true;
	}

	@Override
	public boolean visit(IntersectionType n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(JavadocComment n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(LabeledStmt n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(LambdaExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(LineComment n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(LocalClassDeclarationStmt n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(LocalRecordDeclarationStmt n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(LongLiteralExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(MarkerAnnotationExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(MemberValuePair n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(MethodCallExpr n)
	{
		genericVisit(n);
		Resource method_node = m_parents.peek();
		Literal name_node = m_model.createLiteral(n.getName().asString());
		m_model.add(method_node, NAME, name_node);
		Resource arg_node = m_model.createResource();
		m_model.add(method_node, ARGUMENTS, arg_node);
		for (Node a : n.getArguments())
		{
			AstToRdfVisitor arg_visitor = new AstToRdfVisitor(m_model, m_index, arg_node);
			PushPopVisitableNode to_explore = new PushPopVisitableNode(a);
			to_explore.accept(arg_visitor);
		}
		return false;
	}

	@Override
	public boolean visit(MethodDeclaration n)
	{
		genericVisit(n);
		Resource method_node = m_parents.peek();
		List<Node> children = n.getChildNodes();
		Literal name_node = m_model.createLiteral(children.get(0).toString());
		m_model.add(method_node, NAME, name_node);
		{
			AstToRdfVisitor ret_visitor = new AstToRdfVisitor(m_model, m_index, null);
			PushPopVisitableNode to_explore = new PushPopVisitableNode(children.get(1));
			to_explore.accept(ret_visitor);
			m_model.add(method_node, RETURNS, ret_visitor.getRoot());
		}
		{
			AstToRdfVisitor body_visitor = new AstToRdfVisitor(m_model, m_index, method_node);
			PushPopVisitableNode to_explore = new PushPopVisitableNode(children.get(children.size() - 1));
			to_explore.accept(body_visitor);
			//m_model.add(method_node, IN, body_visitor.getRoot());
		}
		return false;
	}

	@Override
	public boolean visit(MethodReferenceExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(NameExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(Name n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(NormalAnnotationExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(NullLiteralExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ObjectCreationExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(PackageDeclaration n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(Parameter n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(PrimitiveType n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(RecordDeclaration n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(CompactConstructorDeclaration n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ReturnStmt n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(SimpleName n)
	{
		// By design, we ignore SimpleName nodes; those that are relevant
		// are handled in their parent nodes
		//genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(SingleMemberAnnotationExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(StringLiteralExpr n)
	{
		genericVisit(n);
		Resource str_node = m_parents.peek();
		Literal name_node = m_model.createLiteral(n.getValue());
		m_model.add(str_node, VALUE, name_node);
		return true;
	}

	@Override
	public boolean visit(SuperExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(SwitchEntry n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(SwitchStmt n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(SynchronizedStmt n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ThisExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ThrowStmt n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(TryStmt n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(TypeExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(TypeParameter n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(UnaryExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(UnionType n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(UnknownType n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(VariableDeclarator n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(VoidType n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(WhileStmt n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(WildcardType n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ModuleDeclaration n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ModuleRequiresDirective n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ModuleExportsDirective n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ModuleProvidesDirective n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ModuleUsesDirective n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ModuleOpensDirective n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(UnparsableStmt n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(ReceiverParameter n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(VarType n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(Modifier n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(SwitchExpr switchExpr)
	{
		genericVisit(switchExpr);
		return true;
	}

	@Override
	public boolean visit(TextBlockLiteralExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(YieldStmt yieldStmt)
	{
		genericVisit(yieldStmt);
		return true;
	}

	@Override
	public boolean visit(TypePatternExpr n)
	{
		genericVisit(n);
		return true;
	}

	@Override
	public boolean visit(RecordPatternExpr n)
	{
		genericVisit(n);
		return true;
	}

	protected void genericVisit(Node n)
	{
		String iri = AstIds.iriFor("", n);
		Resource rdf_node = m_model.createResource(iri);
		Resource rdf_parent = m_parents.isEmpty() ? null : m_parents.peek();
		if (rdf_parent == null)
		{
			m_root = rdf_node;
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

	protected void genericLeave(Node n)
	{
		if (!m_parents.isEmpty())
		{
			m_parents.pop();
		}
	}

	public Resource getRoot()
	{
		return m_root;
	}

	public boolean visit(PushPopVisitableNode n)
	{
		return true;
	}

	public boolean leave(PushPopVisitableNode n)
	{
		return true;
	}
}
