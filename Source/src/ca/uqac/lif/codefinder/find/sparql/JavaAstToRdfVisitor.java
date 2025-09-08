package ca.uqac.lif.codefinder.find.sparql;

import java.util.List;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

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
		Resource arg_node = m_model.createResource();
		m_model.add(method_node, ARGUMENTS, arg_node);
		m_parents.push(arg_node);
		for (Node a : n.getArguments())
		{
			JavaAstToRdfVisitor arg_visitor = new JavaAstToRdfVisitor(m_model, m_index, arg_node);
			PushPopVisitableNode to_explore = new PushPopVisitableNode(a);
			to_explore.accept(arg_visitor);
		}
		stop();
	}
	
	@Override
	public void leave(MethodCallExpr n)
	{
		genericleave(n);
		m_parents.pop(); // Since we added the "args" node in visit()
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
			JavaAstToRdfVisitor ret_visitor = new JavaAstToRdfVisitor(m_model, m_index, null);
			PushPopVisitableNode to_explore = new PushPopVisitableNode(children.get(1));
			to_explore.accept(ret_visitor);
			m_model.add(method_node, RETURNS, ret_visitor.getRoot());
		}
		{
			JavaAstToRdfVisitor body_visitor = new JavaAstToRdfVisitor(m_model, m_index, method_node);
			PushPopVisitableNode to_explore = new PushPopVisitableNode(children.get(children.size() - 1));
			to_explore.accept(body_visitor);
			//m_model.add(method_node, IN, body_visitor.getRoot());
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
