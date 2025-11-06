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
package ca.uqac.lif.piglet.find;

/**
 * A file filter that accepts files whose code contains a given substring.
 */
public class SubstringFileFilter implements FileFilter
{
	/** The substring to look for */
	protected final String m_substring;

	/**
	 * Creates a new substring file filter.
	 * 
	 * @param substring
	 *          The substring to look for
	 */
	public SubstringFileFilter(String substring)
	{
		super();
		m_substring = substring;
	}

	@Override
	public boolean accept(String code)
	{
		return code.contains(m_substring);
	}
}
