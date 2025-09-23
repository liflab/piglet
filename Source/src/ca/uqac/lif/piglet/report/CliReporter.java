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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import ca.uqac.lif.fs.FilePath;
import ca.uqac.lif.piglet.find.FoundToken;
import ca.uqac.lif.piglet.report.Report.MapReport;
import ca.uqac.lif.piglet.report.Report.ObjectReport;
import ca.uqac.lif.piglet.util.AnsiPrinter;
import ca.uqac.lif.piglet.util.AnsiPrinter.Color;

public class CliReporter implements Reporter
{
	/** The output stream to which the report is sent */
	protected final AnsiPrinter m_out;
	
	/** Whether to display a summary only */
	protected final boolean m_summary;
	
	public CliReporter(AnsiPrinter out, boolean summary)
	{
		super();
		m_out = out;
		m_summary = summary;
	}
	
	protected void setTitle(int level)
	{
		switch (level)
		{
			case 0:
				m_out.setForegroundColor(Color.PURPLE);
				m_out.underline();
				break;
			case 1:
				m_out.setForegroundColor(Color.CYAN);
				m_out.bold();
				break;
			case 2:
				m_out.setForegroundColor(Color.GREEN);
				break;
			default:
				m_out.setForegroundColor(Color.WHITE);
				break;
		}
	}
	
	protected void unsetTitle(int level)
	{
		m_out.unbold();
		m_out.ununderline();
	}
	
	protected void reportRecursive(FilePath root, Report r, String indent, int level)
	{
		if (!(r instanceof MapReport))
		{
			return;
		}
		MapReport mr = (MapReport) r;
		for (Map.Entry<String, Report> e : mr.entrySet())
		{
			String key = e.getKey();
			Report value = e.getValue();
			setTitle(level);
			m_out.print(indent);
			if (level > 0)
			{
				m_out.print(AnsiPrinter.padToLength(key, 36 - (level * 2), true));
				if (value instanceof ObjectReport)
				{
					m_out.print(": ");
				}
			}
			else
			{
				m_out.print(key);
			}
			unsetTitle(level);
			m_out.resetColors();
			if (value instanceof ObjectReport)
			{
				ObjectReport or = (ObjectReport) value;
				if (or.getObject() instanceof List<?>)
				{
					@SuppressWarnings("unchecked")
					List<FoundToken> list = (List<FoundToken>) or.getObject();
					m_out.setForegroundColor(Color.YELLOW);
					m_out.print(AnsiPrinter.padToLength(Integer.toString(list.size()), 4));
					//float percentage = 	100f * list.size() / total;
					//m_out.setForegroundColor(Color.BROWN);
					//m_out.print(" (");
					//m_out.print(String.format("%.1f", percentage));
					//m_out.print("%)");
					m_out.resetColors();
					if (!m_summary)
					{
						m_out.println();
						displayTokens(m_out, list);
						m_out.println();
						continue;
					}
				}
				else
				{
					m_out.setForegroundColor(Color.YELLOW);
				}
				m_out.println();
			}
			else
			{
				m_out.println();
				reportRecursive(root, value, indent + "  ", level + 1);
			}
		}
	}
	
	@Override
	public void report(FilePath root, Report r) throws ReporterException
	{
		reportRecursive(root, r, "", 0);
	}
	
	/**
	 * Displays a list of found tokens to a given output stream.
	 * @param out The output stream
	 * @param found The list of found tokens
	 */
	protected static void displayTokens(AnsiPrinter out, List<FoundToken> found)
	{
		Collections.sort(found);
		for (FoundToken t : found)
		{
			out.print("- ");
			out.println(t);
		}
	}
}
