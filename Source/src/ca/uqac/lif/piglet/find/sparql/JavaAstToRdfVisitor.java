/*
    Analysis of assertions in Java programs
    Copyright (C) 2025 Sylvain Hallé, Sarika Machhindra Kadam

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.piglet.find.sparql;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.model.SymbolReference;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;

import ca.uqac.lif.piglet.find.TokenFinderContext;
import ca.uqac.lif.piglet.find.visitor.PushPopVisitableNode;

public class JavaAstToRdfVisitor extends AstToRdfVisitor
{
	public static final Property PARAMETERS = ResourceFactory.createProperty(ModelBuilder.NS, "params");

	public static final Property VALUE = ResourceFactory.createProperty(ModelBuilder.NS, "value");

	public static final Property RETURNS = ResourceFactory.createProperty(ModelBuilder.NS, "returns");

	public static final Property VARIABLES = ResourceFactory.createProperty(ModelBuilder.NS, "variables");

	public static final Property CONDITION = ResourceFactory.createProperty(ModelBuilder.NS, "condition");

	public static final Property INITIALIZER = ResourceFactory.createProperty(ModelBuilder.NS, "initializer");

	public static final Property SCOPE = ResourceFactory.createProperty(ModelBuilder.NS, "scope");

	public static final Property NEXT = ResourceFactory.createProperty(ModelBuilder.NS, "next");

	public static final Property ARG_1 = ResourceFactory.createProperty(ModelBuilder.NS, "arg1");

	public static final Property ARG_2 = ResourceFactory.createProperty(ModelBuilder.NS, "arg2");

	public static final Property OPERATOR = ResourceFactory.createProperty(ModelBuilder.NS, "operator");

	public static final Property DECLARATION = ResourceFactory.createProperty(ModelBuilder.NS, "declaration");

	public JavaAstToRdfVisitor(int follow, TokenFinderContext context, String filename)
	{
		super(follow, context, filename);
	}

	public JavaAstToRdfVisitor(Model m_model, LazyNodeIndex<Node,String> m_index, Resource method_node, int follow, TokenFinderContext context, String filename)
	{
		super(m_model, m_index, method_node, follow, context, filename);
	}

	@Override
	public void visit(BlockStmt n)
	{
		if (!genericVisit(n)) { stop(); return; }
		Resource block_node = m_parents.peek();
		NodeList<Statement> statements = n.getStatements();
		if (statements.size() == 0)
		{
			stop();
			return;
		}
		Resource prev_node = null;
		for (Statement s : statements)
		{

			JavaAstToRdfVisitor stmt_visitor = new JavaAstToRdfVisitor(m_model, m_index, null, m_follow, m_context, m_filename);
			PushPopVisitableNode to_explore = new PushPopVisitableNode(s);
			to_explore.accept(stmt_visitor);
			Resource stmt_node = stmt_visitor.getRoot();
			if (prev_node == null)
			{
				m_model.add(block_node, IN, stmt_node);
			}
			else
			{
				m_model.add(prev_node, NEXT, stmt_node);
			}
			prev_node = stmt_node;
		}
		stop();
	}

	@Override
	public void visit(MethodCallExpr n)
	{
		if (!genericVisit(n)) { stop(); return; }
		Resource method_node = m_parents.peek();
		Literal name_node = m_model.createLiteral(n.getName().asString());
		m_model.add(method_node, NAME, name_node);
		{
			// Method arguments
			Resource arg_node = m_model.createResource();
			m_model.add(method_node, PARAMETERS, arg_node);
			m_parents.push(arg_node); 
			for (int i = 0; i < n.getArguments().size(); i++)
			{
				Expression a = n.getArgument(i);
				JavaAstToRdfVisitor arg_visitor = new JavaAstToRdfVisitor(m_model, m_index, null, m_follow, m_context, m_filename);
				PushPopVisitableNode to_explore = new PushPopVisitableNode(a);
				to_explore.accept(arg_visitor);
				m_model.add(arg_node, ResourceFactory.createProperty(ModelBuilder.NS, "arg" + (i + 1)), arg_visitor.getRoot());
			}
			m_parents.pop();
		}
		{
			// Scope
			if (n.getScope().isPresent())
			{
				JavaAstToRdfVisitor arg_visitor = new JavaAstToRdfVisitor(m_model, m_index, null, m_follow, m_context, m_filename);
				PushPopVisitableNode to_explore = new PushPopVisitableNode(n.getScope().get());
				to_explore.accept(arg_visitor);
				m_model.add(method_node, SCOPE, arg_visitor.getRoot());
			}
		}
		if (m_follow > 0)
		{
			// Try to resolve the method declaration and explore it
			try
			{
				SymbolReference<ResolvedMethodDeclaration> rmd = JavaParserFacade.get(m_context.getTypeSolver()).solve(n);
				if (rmd.isSolved())
				{
					ResolvedMethodDeclaration md = rmd.getCorrespondingDeclaration();
					if (md.toAst().isPresent())
					{
						String target_filename = getDeclaringFileName(n).orElse("");
						Node root = md.toAst().get();
						PushPopVisitableNode to_explore = new PushPopVisitableNode(root);
						JavaAstToRdfVisitor method_visitor = new JavaAstToRdfVisitor(m_model, m_index, method_node, m_follow - 1, m_context, target_filename);
						to_explore.accept(method_visitor);
						m_model.add(method_node, DECLARATION, method_visitor.getRoot());
					}
				}
			}
			catch (UnsupportedOperationException e)
			{
				// Occurs when the type solver cannot reliably resolve the method;
				// we silently ignore it
				e.printStackTrace();
			}
			catch (UnsolvedSymbolException e)
			{
				// Ignored
				e.printStackTrace();
			}
		}
		stop();
	}

	@Override
	public void visit(FieldAccessExpr n)
	{
		if (!genericVisit(n)) { stop(); return; }
		Resource field_node = m_parents.peek();
		Literal name_node = m_model.createLiteral(n.getName().asString());
		m_model.add(field_node, NAME, name_node);
		{
			// Scope
			Expression e = n.getScope();
			Literal scope_node = m_model.createLiteral(e.toString());
			m_model.add(field_node, SCOPE, scope_node);
		}
		stop();
	}

	@Override
	public void visit(VariableDeclarationExpr n)
	{
		if (!genericVisit(n)) { stop(); return; }
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
			Literal name_node = m_model.createLiteral(v.getNameAsString());
			m_model.add(vars, NAME, name_node);
			if (v.getInitializer().isPresent())
			{
				Expression e = v.getInitializer().get();
				JavaAstToRdfVisitor init_visitor = new JavaAstToRdfVisitor(m_model, m_index, null, m_follow, m_context, m_filename);
				PushPopVisitableNode to_explore = new PushPopVisitableNode(e);
				to_explore.accept(init_visitor);
				m_model.add(vars, INITIALIZER, init_visitor.getRoot());
			}
		});
		stop();
	}

	@Override
	public void visit(FieldDeclaration n)
	{
		if (!genericVisit(n)) { stop(); return; }
		Resource field_node = m_parents.peek();
		NodeList<VariableDeclarator> n_vars = n.getVariables();
		if (n_vars.size() == 0)
		{
			stop();
			return;
		}
		Resource vars = m_model.createResource();
		m_model.add(field_node, VARIABLES, vars);
		n_vars.forEach(v -> {
			Type t = v.getType();
			Literal type_name = m_model.createLiteral(t.asString());
			m_model.add(vars, TYPE, type_name);
			Literal name_node = m_model.createLiteral(v.getNameAsString());
			m_model.add(vars, NAME, name_node);
			if (v.getInitializer().isPresent())
			{
				Expression e = v.getInitializer().get();
				JavaAstToRdfVisitor init_visitor = new JavaAstToRdfVisitor(m_model, m_index, null, m_follow, m_context, m_filename);
				PushPopVisitableNode to_explore = new PushPopVisitableNode(e);
				to_explore.accept(init_visitor);
				m_model.add(vars, INITIALIZER, init_visitor.getRoot());
			}
		});
		stop();
	}

	@Override
	public void visit(IfStmt n)
	{
		if (!genericVisit(n)) { stop(); return; }
		Resource if_node = m_parents.peek();
		// Condition
		{
			Expression e = n.getCondition();
			JavaAstToRdfVisitor cond_visitor = new JavaAstToRdfVisitor(m_model, m_index, null, m_follow, m_context, m_filename);
			PushPopVisitableNode to_explore = new PushPopVisitableNode(e);
			to_explore.accept(cond_visitor);
			m_model.add(if_node, CONDITION, cond_visitor.getRoot());
		}
		{
			// Then branch
			Statement b = n.getThenStmt();
			Resource then_node = m_model.createResource();
			m_model.add(if_node, IN, then_node);
			m_model.add(then_node, NODETYPE, m_model.createLiteral("ThenExpr"));
			JavaAstToRdfVisitor then_visitor = new JavaAstToRdfVisitor(m_model, m_index, null, m_follow, m_context, m_filename);
			PushPopVisitableNode to_explore = new PushPopVisitableNode(b);
			to_explore.accept(then_visitor);
			m_model.add(then_node, IN, then_visitor.getRoot());
		}
		{
			// Else branch
			if (n.getElseStmt().isPresent())
			{
				Statement b = n.getElseStmt().get();
				Resource else_node = m_model.createResource();
				m_model.add(if_node, IN, else_node);
				m_model.add(else_node, NODETYPE, m_model.createLiteral("ElseExpr"));
				JavaAstToRdfVisitor else_visitor = new JavaAstToRdfVisitor(m_model, m_index, null, m_follow, m_context, m_filename);
				PushPopVisitableNode to_explore = new PushPopVisitableNode(b);
				to_explore.accept(else_visitor);
				m_model.add(else_node, IN, else_visitor.getRoot());
			}
		}
		stop();
	}

	@Override
	public void visit(BinaryExpr n)
	{
		if (!genericVisit(n)) { stop(); return; }
		Resource bin_node = m_parents.peek();
		Literal name_node = m_model.createLiteral(n.getOperator().asString());
		m_model.add(bin_node, OPERATOR, name_node);
		{
			// Left operand
			Expression left = n.getLeft();
			JavaAstToRdfVisitor left_visitor = new JavaAstToRdfVisitor(m_model, m_index, null, m_follow, m_context, m_filename);
			PushPopVisitableNode to_explore = new PushPopVisitableNode(left);
			to_explore.accept(left_visitor);
			m_model.add(bin_node, ARG_1, left_visitor.getRoot());
		}
		{
			// Right operand
			Expression right = n.getRight();
			JavaAstToRdfVisitor right_visitor = new JavaAstToRdfVisitor(m_model, m_index, null, m_follow, m_context, m_filename);
			PushPopVisitableNode to_explore = new PushPopVisitableNode(right);
			to_explore.accept(right_visitor);
			m_model.add(bin_node, ARG_2, right_visitor.getRoot());
		}
		stop();
	}

	@Override
	public void visit(UnaryExpr n)
	{
		if (!genericVisit(n)) { stop(); return; }
		Resource bin_node = m_parents.peek();
		Literal name_node = m_model.createLiteral(n.getOperator().asString());
		m_model.add(bin_node, OPERATOR, name_node);
		{
			// Left operand
			Expression left = n.getExpression();
			JavaAstToRdfVisitor left_visitor = new JavaAstToRdfVisitor(m_model, m_index, null, m_follow, m_context, m_filename);
			PushPopVisitableNode to_explore = new PushPopVisitableNode(left);
			to_explore.accept(left_visitor);
			m_model.add(bin_node, ARG_1, left_visitor.getRoot());
		}
		stop();
	}



	@Override
	public void visit(ClassOrInterfaceDeclaration n)
	{
		if (!genericVisit(n)) { stop(); return; }
		Resource class_node = m_parents.peek();
		Literal name_node = m_model.createLiteral(n.getNameAsString());
		m_model.add(class_node, NAME, name_node);
	}				

	@Override
	public void visit(MethodDeclaration n)
	{
		if (!genericVisit(n)) { stop(); return; }
		Resource method_node;
		if (m_parents.isEmpty())
		{
			method_node = null;
		}
		else
		{
			method_node = m_parents.peek();
		}
		Literal name_node = m_model.createLiteral(n.getNameAsString());
		if (method_node != null)
		{
			m_model.add(method_node, NAME, name_node);
		}
		{
			// Method parameters
			NodeList<com.github.javaparser.ast.body.Parameter> params = n.getParameters();
			if (params.size() > 0)
			{
				Resource param_node = m_model.createResource();
				m_model.add(method_node, PARAMETERS, param_node);
				m_parents.push(param_node);
				for (com.github.javaparser.ast.body.Parameter p : params)
				{
					JavaAstToRdfVisitor param_visitor = new JavaAstToRdfVisitor(m_model, m_index, param_node, m_follow, m_context, m_filename);
					PushPopVisitableNode to_explore = new PushPopVisitableNode(p);
					to_explore.accept(param_visitor);
				}
				m_parents.pop();
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
			JavaAstToRdfVisitor body_visitor = new JavaAstToRdfVisitor(m_model, m_index, null, m_follow, m_context, m_filename);
			PushPopVisitableNode to_explore = new PushPopVisitableNode(b);
			to_explore.accept(body_visitor);
			m_model.add(method_node, IN, body_visitor.getRoot());
		}
		stop();
	}
	
	@Override
	public void visit(LineComment n)
	{
		if (!genericVisit(n)) { stop(); return; }
		Resource lc_node = m_parents.peek();
		Literal name_node = m_model.createLiteral(n.asString());
		m_model.add(lc_node, NAME, name_node);
		stop();
	}

	@Override
	public void visit(IntegerLiteralExpr n)
	{
		if (!genericVisit(n)) { stop(); return; }
		Resource int_node = m_parents.peek();
		Literal name_node = m_model.createLiteral(n.getValue());
		m_model.add(int_node, NAME, name_node);
		m_model.add(int_node, TYPE, m_model.createLiteral("Integer"));
	}

	@Override
	public void visit(BooleanLiteralExpr n)
	{
		if (!genericVisit(n)) { stop(); return; }
		Resource bool_node = m_parents.peek();
		Literal name_node = m_model.createLiteral(Boolean.toString(n.getValue()));
		m_model.add(bool_node, NAME, name_node);
		m_model.add(bool_node, TYPE, m_model.createLiteral("Boolean"));
		stop();
	}

	@Override
	public void visit(StringLiteralExpr n)
	{
		if (!genericVisit(n)) { stop(); return; }
		Resource str_node = m_parents.peek();
		Literal name_node = m_model.createLiteral(n.getValue());
		m_model.add(str_node, NAME, name_node);
		m_model.add(str_node, TYPE, m_model.createLiteral("String"));
		stop();
	}

	@Override
	public void visit(NullLiteralExpr n)
	{
		if (!genericVisit(n)) { stop(); return; }
		Resource null_node = m_parents.peek();
		Literal name_node = m_model.createLiteral("null");
		m_model.add(null_node, NAME, name_node);
		stop();
	}

	@Override
	public void visit(NameExpr n)
	{
		if (!genericVisit(n)) { stop(); return; }
		Literal name_node = m_model.createLiteral(n.getName().asString());
		m_model.add(m_parents.peek(), NAME, name_node);
		stop();
	}
}
