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
package ca.uqac.lif.piglet.report;

import static org.codelibs.jhighlight.renderer.XhtmlRendererFactory.JAVA;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.codelibs.jhighlight.renderer.Renderer;
import org.codelibs.jhighlight.renderer.XhtmlRendererFactory;

import ca.uqac.lif.fs.FilePath;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.FileUtils;
import ca.uqac.lif.piglet.find.FoundToken;
import ca.uqac.lif.piglet.report.Report.MapReport;
import ca.uqac.lif.piglet.report.Report.ObjectReport;
import ca.uqac.lif.piglet.util.Paths;

/**
 * A reporter that outputs the results of a code search in HTML format.
 */
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
	public void report(FilePath root, Report r, Map<String,Long> timeouts) throws ReporterException
	{
		if (!(r instanceof MapReport))
		{
			throw new ReporterException("Expected a map report");
		}
		try
		{
			FileUtils.copy(HtmlReporter.class.getResourceAsStream("header.html"), m_out);
			printSummary(root, (MapReport) r);
			reportTokensRecursive(root, (MapReport) r, 0);
			FileUtils.copy(HtmlReporter.class.getResourceAsStream("footer.html"), m_out);
		}
		catch (FileSystemException e)
		{
			throw new ReporterException(e);
		}
	}

	/**
	 * Prints a summary of the found tokens
	 * @param root The root directory of the search
	 * @param total The total number of tokens searched for
	 * @param found A map associating file paths to lists of found tokens
	 * @param unresolved A set of unresolved symbols, or <tt>null</tt> if
	 * nothing to show
	 */
	protected void printSummary(FilePath root, MapReport global) throws ReporterException
	{
		m_out.println("<section>");
		for (Map.Entry<String,Report> p_e : global.entrySet())
		{
			String project = p_e.getKey();
			m_out.println("<h2>Summary for " + project + "</h2>");
			printSummaryRecursive(root, (MapReport) p_e.getValue());
		}
		m_out.println("</section>");
	}
	
	protected void printSummaryRecursive(FilePath root, MapReport found) throws ReporterException
	{
		m_out.println("<ul>");
		for (Map.Entry<String,Report> e : found.entrySet())
		{
			if (e.getValue() instanceof MapReport)
			{
				m_out.println("<li>" + e.getKey());
				printSummaryRecursive(root, (MapReport) e.getValue());
				m_out.println("</li>");
			}
			else if (e.getValue() instanceof ObjectReport)
			{
				ObjectReport or = (ObjectReport) e.getValue();
				List<?> in_list = (List<?>) or.getObject();
				m_out.println("<li><a href=\"#" + e.getKey() + "\">" + e.getKey() + "</a> (" + in_list.size() + ")</li>");
			}
		}
		m_out.println("</ul>");
	}
	
	protected void reportTokensRecursive(FilePath root, MapReport found, int level) throws ReporterException
	{
		for (Map.Entry<String,Report> e : found.entrySet())
		{
			m_out.println("<section>");
			if (level > 0)
			{
				m_out.println("<a name=\"" + e.getKey() + "\"></a>");
				m_out.println("<h" + (level + 2) + ">" + e.getKey() + " (" + "foo" + ")</h" + (level + 2) + ">");
			}
			if (e.getValue() instanceof MapReport)
			{
				reportTokensRecursive(root, (MapReport) e.getValue(), level + 1);
			}
			else if (e.getValue() instanceof ObjectReport)
			{
				ObjectReport or = (ObjectReport) e.getValue();
				List<?> in_list = (List<?>) or.getObject();
				reportTokens(root, in_list);
			}
			m_out.println("</section>");
		}
	}

	/**
	 * Reports a list of found tokens to the output stream
	 * @param root The root directory of the search
	 * @param out The output stream to which the report is sent
	 * @param found The list of found tokens
	 */
	protected void reportTokens(FilePath root, List<?> found)
	{
		m_out.println("<dl>");
		TreeSet<FoundToken> ts = new TreeSet<>();
		for (Object o : found)
		{
			if (!(o instanceof FoundToken))
			{
				continue;
			}
			FoundToken t = (FoundToken) o;
			ts.add(t);
		}
		Renderer rend = XhtmlRendererFactory.getRenderer(JAVA);
		Iterator<FoundToken> it = ts.iterator();
		while (it.hasNext())
		{
			FoundToken t = (FoundToken) it.next();
			String clear_fn = t.getFilename().substring(1);
			FilePath folder = root.chdir(Paths.getPathOfFile(clear_fn));
			m_out.print("<dt><a href=\"");
			m_out.print(folder + FilePath.SLASH + Paths.getFilename(clear_fn));
			m_out.print("\">");
			m_out.print(clear_fn);
			m_out.print("</a> <span class=\"lines\">");
			m_out.print(t.getLocation());
			m_out.println("</span></dt>");
			String code = t.getSnippet();
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
}
