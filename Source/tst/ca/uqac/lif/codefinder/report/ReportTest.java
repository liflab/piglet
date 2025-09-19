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

import org.junit.Test;

import ca.uqac.lif.codefinder.report.Report.MapReport;
import ca.uqac.lif.codefinder.report.Report.ObjectReport;

/**
 * Unit tests for the classes in the <tt>ca.uqac.lif.codefinder.report</tt>.
 */
public class ReportTest
{
	@Test
	public void test()
	{
		MapReport r = new MapReport();
		r.put("a", new ObjectReport(1));
		r.put("b/c", new ObjectReport(2));
		r.put("b/d", new ObjectReport(3));
		System.out.println(r);
	}
}
