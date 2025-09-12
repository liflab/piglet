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
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.uqac.lif.codefinder.find.FoundToken;
import ca.uqac.lif.fs.FilePath;

/**
 * An interface for objects that generate reports of the results of a code
 * search.
 */
public interface Reporter
{
	/**
	 * Generates a report of the found tokens.
	 * @param root The root directory where the search was performed
	 * @param total The total number of tokens searched for
	 * @param found A map associating file paths to lists of found tokens
	 * @param unresolved A set of unresolved symbols, or <tt>null</tt> if
	 * nothing to show
	 * @throws IOException If an error occurs while writing the report
	 */
	public void report(FilePath root, int total, Map<String,List<FoundToken>> found, Set<String> unresolved) throws IOException;
}
