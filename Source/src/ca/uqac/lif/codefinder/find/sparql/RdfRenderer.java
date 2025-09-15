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

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.PrefixMapping;

import java.io.PrintStream;
import java.util.*;

/**
 * Renders an RDF model as a DOT graph.
 */
public class RdfRenderer
{
	private final Model model;
	private final Map<Node, String> idMap = new HashMap<>(); // only for non-literals
	private int anonCounter = 0;
	private int litCounter = 0;

	public RdfRenderer(Model model)
	{
		this.model = model;
	}

	/**
	 * Produces a DOT representation of the RDF model.
	 */
	public String toDot(PrintStream ps)
	{
		StringBuilder sb = new StringBuilder();
		ps.print("digraph G {\n");
		// Simple, readable defaults
		ps.print("  graph [rankdir=LR];\n");
		ps.print("  node  [shape=circle, fontsize=10,fontname=\"DejaVu Sans Mono\"];\n");
		ps.print("  edge  [fontsize=9];\n\n");

		// 1) Declare all resource/blank nodes (we’ll declare literal nodes per-edge
		// later)
		// Collect distinct non-literal nodes that appear in S or O
		Set<Node> resourceNodes = new LinkedHashSet<>();
		List<Statement> stmts = model.listStatements().toList();
		for (Statement st : stmts)
		{
			resourceNodes.add(st.getSubject().asNode());
			RDFNode obj = st.getObject();
			if (!obj.isLiteral())
			{
				resourceNodes.add(obj.asNode());
			}
		}

		for (Node node : resourceNodes)
		{
			String id = idForNonLiteral(node); // stable id for resources & blank nodes
			String label = labelFor(node);
			String shape = "circle";
			String edge_color = getBorderColor(label);
			ps.print("  " + id + " [style=filled,fillcolor=white,color=\"" + edge_color + "\",label=\"" + escape(label) + "\", fixedsize=true,width=.3,shape=" + shape + "];\n");
		}
		ps.print("\n");

		// 2) Emit edges; if object is a literal, create a brand-new node for each
		// statement
		for (Statement st : stmts)
		{
			Node sN = st.getSubject().asNode();
			Property p = st.getPredicate();
			RDFNode o = st.getObject();

			String sId = idForNonLiteral(sN);
			String edgeLabel = labelForPredicate(p);

			String oId;
			if (o.isLiteral())
			{
				// Create a unique literal node for THIS statement (no sharing)
				oId = newLiteralId();
				String litLabel = labelForLiteral(o.asLiteral());
				String color = getNodeColor(p.toString());
				ps.print("  " + oId + " [style=filled,fillcolor=\"" + color + "\",label=\"" + escape(litLabel) + "\", shape=box,height=.3];\n");
			}
			else
			{
				oId = idForNonLiteral(o.asNode());
			}

			ps.print("  " + sId + " -> " + oId + " " + formatEdge(escape(edgeLabel)) + ";\n");
		}
		ps.print("}\n");
		return sb.toString();
	}

	protected static String formatEdge(String predicate)
	{
		String text = "";
		String color = "";
		switch (predicate)
		{
		case "nodetype":
			text = "τ";
		color = "#800080"; // purple"
		break;
		case "name":
			text = "ν";
		color = "#338000"; // dark green
		break;
		case "scope":
			text = "σ";
		color = "#5fbcd3"; // light blue
		break;
		case "in":
			text = "∈";
		color = "#800000";	 // dark red	
		break;
		case "arg1":
			text = "1";
		color = "#786721"; // orange
		break;
		case "arg2":
			text = "2";
		color = "#786721"; // orange
		break;
		case "operator":
			text = "o";
		color = "#5555ff";	 // blue
		break;
		case "annotation":
			text = "α";
		color = "#ff00ff";	 // magenta
		break;
		case "modifiers":
			text = "μ";
		color = "#d45500";	 // orange
		break;
		case "declaration":
			text = "δ";
		color = "#800000";	
		break;
		case "initializer":
			text = "ι";
		color = "#00aaaa"; // teal
		break;
	case "params":
		text = "π";
		color = "#00aaaa"; // teal
		break;
	case "returns":
		text = "ρ";
		color = "#ff0000"; // teal
			break;
		default:
			text = predicate;
			color = "black";
			break;
		}
		return "[label=\"" + text + "\",color=black,fontcolor=\"" + color + "\"]";
	}

	protected static String getNodeColor(String predicate)
	{
		switch (predicate)
		{
		case ModelBuilder.NS + "nodetype":
			return "#fff6d5"; // light yellow
		case ModelBuilder.NS + "name":
			return "#ffd5d5"; // light red
		case ModelBuilder.NS + "scope":
		case ModelBuilder.NS + "resolvedtype":
		case ModelBuilder.NS + "returns":
			return "#e3f4d7"; // light green
		case ModelBuilder.NS:
			return "white";
		default:
			return "white";
		}
	}

	protected static String getBorderColor(String predicate)
	{
		switch (predicate)
		{
		case "":
			return "#00ff00"; // green
		default:
			return "black";
		}
	}

	/** Assigns a stable DOT id for non-literal nodes (resources, blank nodes). */
	private String idForNonLiteral(Node n)
	{
		// identity-based map so identical blank nodes get same id; IRIs share by value
		// object
		String id = idMap.get(n);
		if (id != null) return id;

		if (n.isBlank()) {
			id = "bn" + (anonCounter++);
		} else if (n.isURI()) {
			id = "u_" + safeIdFromString(n.getURI());
		} else {
			id = "n_" + safeIdFromString(n.toString());
		}
		idMap.put(n, id);
		return id;
	}

	/** Produces a fresh DOT id for a literal node (never shared). */
	private String newLiteralId()
	{
		return "lit" + (litCounter++);
	}

	/** Human-readable label for any node. */
	private String labelFor(Node n) {
		if (n.isBlank()) {
			return ""; //"_:" + n.getBlankNodeLabel();
		}
		if (n.isURI()) {
			String uri = n.getURI();
			if (uri.startsWith("urn:ast")) return shortenAst(uri);
			String q = qnameOrNull(uri);
			if (q != null) return q;
			String frag = localFragment(uri);
			return (frag != null) ? frag : uri;
		}
		return n.toString();
	}

	/** Label for predicates: prefer QName, else local fragment, else full URI. */
	private String labelForPredicate(Property p)
	{
		String uri = p.getURI();
		String q = qnameOrNull(uri);
		if (q != null)
			return q;
		String frag = localFragment(uri);
		return (frag != null) ? frag : uri;
	}

	/**
	 * Label for literals: use lexical form; include @lang or ^^datatype if present.
	 */
	private String labelForLiteral(Literal lit)
	{
		return lit.getLexicalForm();
	}

	/**
	 * For "urn:ast..." IRIs: extract first 3 hex chars from the trailing hex run;
	 * else fallback.
	 */
	private String shortenAst(String uri)
	{
		// Heuristic: take the last run of hex digits in the URI and keep first 3
		// Examples:
		// urn:ast:1a2b3c... -> 1a2
		// urn:ast#deadbeef -> dea
		String tail = uri;
		int hash = uri.lastIndexOf('#');
		int colon = uri.lastIndexOf(':');
		int sep = Math.max(hash, colon);
		if (sep >= 0 && sep < uri.length() - 1)
		{
			tail = uri.substring(sep + 1);
		}
		String hexRun = longestTrailingHexRun(tail);
		if (!hexRun.isEmpty())
		{
			String three = hexRun.substring(0, Math.min(3, hexRun.length()));
			return three;
		}
		// Fallback to last fragment
		String frag = localFragment(uri);
		return (frag != null) ? frag : uri;
	}

	private String longestTrailingHexRun(String s)
	{
		int end = s.length();
		int i = end - 1;
		while (i >= 0 && isHex(s.charAt(i)))
			i--;
		String run = s.substring(i + 1, end);
		// If the trailing run is empty (no hex at very end), try the longest run
		// anywhere
		if (run.isEmpty())
		{
			int bestLen = 0, bestStart = -1;
			int start = -1;
			for (int j = 0; j < s.length(); j++)
			{
				if (isHex(s.charAt(j)))
				{
					if (start == -1)
						start = j;
				}
				else if (start != -1)
				{
					int len = j - start;
					if (len > bestLen)
					{
						bestLen = len;
						bestStart = start;
					}
					start = -1;
				}
			}
			if (start != -1)
			{ // run till end
				int len = s.length() - start;
				if (len > bestLen)
				{
					bestLen = len;
					bestStart = start;
				}
			}
			return (bestStart >= 0) ? s.substring(bestStart, bestStart + bestLen) : "";
		}
		return run;
	}

	private boolean isHex(char c)
	{
		return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
	}

	/**
	 * Try to turn URI into a QName using model prefixes; return null if not
	 * possible.
	 */
	private String qnameOrNull(String uri)
	{
		if (uri == null)
			return null;
		PrefixMapping pm = model;
		String q = pm.qnameFor(uri);
		return (q != null && q.contains(":")) ? q : null;
	}

	/** Return fragment after '#' or last '/' or ':'; null if none. */
	private String localFragment(String uri)
	{
		if (uri == null)
			return null;
		int i = Math.max(uri.lastIndexOf('#'), Math.max(uri.lastIndexOf('/'), uri.lastIndexOf(':')));
		if (i >= 0 && i < uri.length() - 1)
			return uri.substring(i + 1);
		return null;
	}

	/** Escape label text for DOT. */
	private String escape(String s)
	{
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	/** Make a safe DOT id: letters, digits, and underscores only. */
	private String safeIdFromString(String s)
	{
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if (Character.isLetterOrDigit(c))
				b.append(c);
			else
				b.append('_');
		}
		// DOT ids cannot start with a digit if unquoted; prepend if needed
		if (b.length() == 0 || Character.isDigit(b.charAt(0)))
		{
			b.insert(0, "n_");
		}
		return b.toString();
	}
}
