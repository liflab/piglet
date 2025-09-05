package ca.uqac.lif.codefinder.sparql;

import org.apache.jena.rdf.model.Model;

import ca.uqac.lif.codefinder.ast.PushPopVisitableNode;

public class ModelBuilder
{
	/** Namespace for the vocabulary */
	public static final String NS = "http://liflab.uqac.ca/";

	public static ModelBuilderResult buildModel(PushPopVisitableNode n)
	{
		AstToRdfVisitor visitor = new AstToRdfVisitor();
		n.accept(visitor);
		return new ModelBuilderResult(visitor.getModel(), visitor.getIndex());
	}

	public static class ModelBuilderResult
	{
		protected final Model m_model;

		protected final JavaAstNodeIndex m_index;

		public ModelBuilderResult(Model model, JavaAstNodeIndex index)
		{
			super();
			m_model = model;
			m_index = index;
		}

		public Model getModel()
		{
			return m_model;
		}

		public JavaAstNodeIndex getIndex()
		{
			return m_index;
		}
	}


}
