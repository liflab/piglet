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
package ca.uqac.lif.codefinder.find.sparql;

import org.apache.jena.rdf.model.Model;

import com.github.javaparser.ast.expr.Expression;

import ca.uqac.lif.codefinder.find.TokenFinderContext;
import ca.uqac.lif.codefinder.find.visitor.PushPopVisitableNode;

public class ModelBuilder
{
	/** Namespace for the vocabulary */
	public static final String NS = "http://liflab.uqac.ca/codefinder#";

	public static ModelBuilderResult buildModel(PushPopVisitableNode n, int follow, TokenFinderContext context)
	{
		JavaAstToRdfVisitor visitor = new JavaAstToRdfVisitor(follow, context);
		n.accept(visitor);
		return new ModelBuilderResult(visitor.getModel(), visitor.getIndex());
	}

	public static class ModelBuilderResult
	{
		protected final Model m_model;

		protected final LazyNodeIndex<Expression,String> m_index;

		public ModelBuilderResult(Model model, LazyNodeIndex<Expression,String> index)
		{
			super();
			m_model = model;
			m_index = index;
		}

		public Model getModel()
		{
			return m_model;
		}

		public LazyNodeIndex<Expression,String> getIndex()
		{
			return m_index;
		}
	}


}
