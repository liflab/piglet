package ca.uqac.lif.codefinder.find.sparql;

import java.util.List;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.type.Type;

import ca.uqac.lif.codefinder.find.ast.PushPopVisitableNode;

public class JavaAstToRdfVisitor extends AstToRdfVisitor
{
	public JavaAstToRdfVisitor()
	{
		super();
	}

	public JavaAstToRdfVisitor(Model m_model, JavaAstNodeIndex m_index, Resource method_node)
	{
		super(m_model, m_index, method_node);
	}

	@Override
	public void visit(MethodCallExpr n)
	{
		genericvisit(n);
		Resource method_node = m_parents.peek();
		Literal name_node = m_model.createLiteral(n.getName().asString());
		m_model.add(method_node, NAME, name_node);
		{
			// Method arguments
			Resource arg_node = m_model.createResource();
			m_model.add(method_node, ARGUMENTS, arg_node);
			m_parents.push(arg_node);
			for (Node a : n.getArguments())
			{
				JavaAstToRdfVisitor arg_visitor = new JavaAstToRdfVisitor(m_model, m_index, arg_node);
				PushPopVisitableNode to_explore = new PushPopVisitableNode(a);
				to_explore.accept(arg_visitor);
			}
			m_parents.pop();
		}
		stop();
	}
	
	@Override
	public void visit(VariableDeclarationExpr n)
	{
		genericvisit(n);
		Resource var_node = m_parents.peek();
		NodeList<VariableDeclarator> n_vars = n.getVariables();
		if (n_vars.size() == 0)
		{
			stop();
			return;
		}
		Resource vars = m_model.createResource();
		m_model.add(var_node, VARIABLES, vars);
		n_vars.forEach(v -> {
			Type t = v.getType();
			Literal type_name = m_model.createLiteral(t.asString());
			m_model.add(vars, TYPE, type_name);
		});
		stop();
	}
	
	@Override
	public void visit(IfStmt n)
	{
		genericvisit(n);
	}

	@Override
	public void visit(MethodDeclaration n)
	{
		genericvisit(n);
		Resource method_node = m_parents.peek();
		List<Node> children = n.getChildNodes();
		Literal name_node = m_model.createLiteral(children.get(0).toString());
		m_model.add(method_node, NAME, name_node);
		{
			// Annotations
			NodeList<AnnotationExpr> annotations = n.getAnnotations();
			if (annotations.size() > 0)
			{
				Resource ann_node = m_model.createResource();
				m_model.add(method_node, ANNOTATIONS, ann_node);
				n.getAnnotations().forEach(a -> {
					Literal ann_name = m_model.createLiteral(a.getName().asString());
					m_model.add(ann_node, NAME, ann_name);
				});
			}
		}
		{
			// Modifiers
			NodeList<Modifier> modifiers = n.getModifiers();
			if (modifiers.size() > 0)
			{
				Resource mod_node = m_model.createResource();
				m_model.add(method_node, MODIFIERS, mod_node);
				n.getModifiers().forEach(m -> {
					Literal mod_name = m_model.createLiteral(m.getKeyword().asString());
					m_model.add(mod_node, NAME, mod_name);
				});
			}
		}
		{
			// Return type
			Type t = n.getType();
			Literal ret_name = m_model.createLiteral(t.asString());
			m_model.add(method_node, RETURNS, ret_name);
		}
		{
			// Method body
			BlockStmt b = n.getBody().orElse(null);
			if (b == null)
			{
				stop();
				return;
			}
			
			JavaAstToRdfVisitor body_visitor = new JavaAstToRdfVisitor(m_model, m_index, method_node);
			PushPopVisitableNode to_explore = new PushPopVisitableNode(b);
			to_explore.accept(body_visitor);
			m_model.add(method_node, IN, body_visitor.getRoot());
		}
		stop();
	}

	@Override
	public void visit(IntegerLiteralExpr n)
	{
		genericvisit(n);
		Resource int_node = m_parents.peek();
		Literal name_node = m_model.createLiteral(n.getValue());
		m_model.add(int_node, VALUE, name_node);
	}

	@Override
	public void visit(BooleanLiteralExpr n)
	{
		genericvisit(n);
		Resource bool_node = m_parents.peek();
		Literal name_node = m_model.createLiteral(Boolean.toString(n.getValue()));
		m_model.add(bool_node, VALUE, name_node);
		stop();
	}

	@Override
	public void visit(StringLiteralExpr n)
	{
		genericvisit(n);
		Resource str_node = m_parents.peek();
		Literal name_node = m_model.createLiteral(n.getValue());
		m_model.add(str_node, VALUE, name_node);
		stop();
	}

	@Override
	public void visit(NameExpr n)
	{
		genericvisit(n);
		Literal name_node = m_model.createLiteral(n.getName().asString());
		m_model.add(m_parents.peek(), NAME, name_node);
		stop();
	}
}
