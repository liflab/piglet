package ca.uqac.lif.codefinder.report;

import static org.codelibs.jhighlight.renderer.XhtmlRendererFactory.JAVA;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import org.codelibs.jhighlight.renderer.Renderer;
import org.codelibs.jhighlight.renderer.XhtmlRendererFactory;

import ca.uqac.lif.codefinder.Main;
import ca.uqac.lif.codefinder.assertion.AnyAssertionFinder;
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
	
	public void report(FilePath root, List<FoundToken> found)
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
				m_out.println("<dd><pre>" + html + "</pre></dd>");
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		m_out.println("</dl>");
	}
	
	protected static void createReport(FilePath root, PrintStream out, Map<String,List<FoundToken>> found) throws IOException
	{ 
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<head>");
		printHighlightCss(out);
		out.println("</head>");
		out.println("<body>");
		out.println("<h2>Summary</h2>");
		out.println("<ul>");
		for (Map.Entry<String, List<FoundToken>> e : found.entrySet())
		{
			out.println("<li><a href=\"#" + e.getKey() + "\">" + e.getKey() + "</a> (" + e.getValue().size() + ")</li>");
		}
		out.println("</ul>");
		for (Map.Entry<String, List<FoundToken>> e : found.entrySet())
		{
			if (e.getKey().compareTo(AnyAssertionFinder.NAME) != 0)
			{
				out.println("<h2><a name=\"" + e.getKey() + "\"></a>" + e.getKey() + " (" + e.getValue().size() + ")</h2>");
				reportTokens(root, out, e.getValue());
			}
		}
		out.println("</body>");
		out.println("</html>");
	}

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
