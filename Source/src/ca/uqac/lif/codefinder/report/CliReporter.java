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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.uqac.lif.codefinder.find.FoundToken;
import ca.uqac.lif.codefinder.util.AnsiPrinter;
import ca.uqac.lif.codefinder.util.AnsiPrinter.Color;
import ca.uqac.lif.fs.FilePath;

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
	
	@Override
	public void report(FilePath root, int total, Map<String,List<FoundToken>> found, Set<String> unresolved) throws IOException
	{
		for (Map.Entry<String, List<FoundToken>> e : found.entrySet())
		{
			m_out.print(AnsiPrinter.padToLength(e.getKey(), 36));
			m_out.setForegroundColor(Color.DARK_GRAY);
			m_out.print(": ");
			m_out.setForegroundColor(Color.YELLOW);
			m_out.print(AnsiPrinter.padToLength(Integer.toString(e.getValue().size()), 4));
			float percentage = 	100f * e.getValue().size() / total;
			m_out.setForegroundColor(Color.BROWN);
			m_out.print(" (");
			m_out.print(String.format("%.1f", percentage));
			m_out.print("%)");
			m_out.println();
			m_out.resetColors();
			if (!m_summary)
			{
				displayTokens(m_out, e.getValue());
				m_out.println();
			}
		}
		if (unresolved != null)
		{
			m_out.print(AnsiPrinter.padToLength("Unresolved symbols", 36));
			m_out.setForegroundColor(Color.DARK_GRAY);
			m_out.print(": ");
			m_out.setForegroundColor(Color.RED);
			m_out.println(unresolved.size());
			m_out.resetColors();
		}
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
