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
package ca.uqac.lif.codefinder.report;

import static org.codelibs.jhighlight.renderer.XhtmlRendererFactory.JAVA;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codelibs.jhighlight.renderer.Renderer;
import org.codelibs.jhighlight.renderer.XhtmlRendererFactory;

import ca.uqac.lif.codefinder.Main;
import ca.uqac.lif.codefinder.assertion.FoundToken;
import ca.uqac.lif.fs.FilePath;

public class HtmlReporter implements Reporter
{
	/** The output stream to which the report is sent */
	protected final PrintStream m_out;
	
	public HtmlReporter(PrintStream out)
	{
		super();
		m_out = out;
	}
	
	@Override
	public void report(FilePath root, Map<String,List<FoundToken>> found, Set<String> unresolved) throws IOException
	{ 
		m_out.println("<!DOCTYPE html>");
		m_out.println("<html>");
		m_out.println("<head>");
		m_out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
		m_out.println("<title>CodeFinder Report</title>");
		printHighlightCss(m_out);
		m_out.println("</head>");
		m_out.println("<body>");
		m_out.println("<h2>Summary</h2>");
		m_out.println("<ul>");
		for (Map.Entry<String, List<FoundToken>> e : found.entrySet())
		{
			m_out.println("<li><a href=\"#" + e.getKey() + "\">" + e.getKey() + "</a> (" + e.getValue().size() + ")</li>");
		}
		m_out.println("<li><a href=\"#unresolved\">Unresolved symbols</a> (" + (unresolved == null ? "0" : unresolved.size()) + ")</li>");
		m_out.println("</ul>");
		for (Map.Entry<String, List<FoundToken>> e : found.entrySet())
		{
			if (!e.getKey().contains("count"))
			{
				m_out.println("<h2><a name=\"" + e.getKey() + "\"></a>" + e.getKey() + " (" + e.getValue().size() + ")</h2>");
				reportTokens(root, e.getValue());
			}
			else
			{
				m_out.println("<h2>" + e.getKey() + " (" + "foo" + ")</h2>");
				reportTokens(root, e.getValue());
			}
		}
		if (unresolved != null)
		{
			m_out.println("<h2><a name=\"unresolved\"></a>Unresolved symbols</h2>");
			m_out.println("<ul>");
			for (String u : unresolved)
			{
				m_out.println("<li><code>" + u + "</code></li>");
			}
			m_out.println("</ul>");
		}
		m_out.println("</body>");
		m_out.println("</html>");
	}
	
	/**
	 * Reports a list of found tokens to the output stream
	 * @param root The root directory of the search
	 * @param out The output stream to which the report is sent
	 * @param found The list of found tokens
	 */
	protected void reportTokens(FilePath root, List<FoundToken> found)
	{
		m_out.println("<dl>");
		for (FoundToken t : found)
		{
			String clear_fn = t.getFilename().substring(1);
			FilePath folder = root.chdir(Main.getPathOfFile(clear_fn));
			m_out.print("<dt><a href=\"");
			m_out.print(folder + FilePath.SLASH + Main.getFilename(clear_fn));
			m_out.print("\">");
			m_out.print(clear_fn);
			m_out.print("</a> ");
			m_out.print(t.getLocation());
			m_out.println("</dt>");
			String code = t.getSnippet();
			Renderer rend = XhtmlRendererFactory.getRenderer(JAVA);
			String html;
			try
			{
				html = rend.highlight("", code, "utf-8", true);
				m_out.println("<dd><code>" + html + "</code></dd>");
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		m_out.println("</dl>");
	}

	/**
	 * Prints CSS rules for syntax highlighting of Java code
	 * @param out The output stream to which the CSS rules are sent
	 */
	protected static void printHighlightCss(PrintStream out)
	{
		out.println("<style type=\"text/css\">\n" + "code {\n"
				+ "color: rgb(0,0,0); font-family: monospace; font-size: 12px; white-space: nowrap;\n"
				+ "}\n" + ".java_plain {\n" + "color: rgb(0,0,0);\n"
				+ "}\n" + ".java_keyword {\n"
				+ "color: rgb(0,0,0); font-weight: bold;\n" + "}\n"
				+ ".java_javadoc_tag {\n"
				+ "color: rgb(147,147,147); background-color: rgb(247,247,247); font-style: italic; font-weight: bold;\n"
				+ "}\n" + "h1 {\n"
				+ "font-family: sans-serif; font-size: 16pt; font-weight: bold; color: rgb(0,0,0); background: rgb(210,210,210); border: solid 1px black; padding: 5px; text-align: center;\n"
				+ "}\n" + ".java_type {\n" + "color: rgb(0,44,221);\n"
				+ "}\n" + ".java_literal {\n" + "color: rgb(188,0,0);\n"
				+ "}\n" + ".java_javadoc_comment {\n"
				+ "color: rgb(147,147,147); background-color: rgb(247,247,247); font-style: italic;\n"
				+ "}\n" + ".java_operator {\n"
				+ "color: rgb(0,124,31);\n" + "}\n"
				+ ".java_separator {\n" + "color: rgb(0,33,255);\n"
				+ "}\n" + ".java_comment {\n"
				+ "color: rgb(147,147,147); background-color: rgb(247,247,247);\n"
				+ "}\n" + "    </style>\n");
	}
}
