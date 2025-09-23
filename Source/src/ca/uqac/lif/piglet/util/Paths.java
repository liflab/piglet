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
package ca.uqac.lif.piglet.util;

import ca.uqac.lif.fs.FilePath;

public class Paths
{
	private Paths()
	{
		// Static class
	}
	
	public static FilePath getPathOfFile(String path)
	{
		int last_slash = path.lastIndexOf('/');
		if (last_slash == -1)
		{
			last_slash = path.lastIndexOf('\\');
		}
		FilePath path_f = null;
		if (last_slash != -1)
		{
			path_f = new FilePath(path.substring(0, last_slash));
		}
		else
		{
			path_f = new FilePath(".");
		}
		if (path_f.toString().isEmpty())
		{
			path_f = new FilePath(".");
		}
		return path_f;
	}

	public static String getFilename(String path)
	{
		int last_slash = path.lastIndexOf('/');
		if (last_slash == -1)
		{
			last_slash = path.lastIndexOf('\\');
		}
		String name = null;
		if (last_slash != -1)
		{
			name = path.substring(last_slash + 1);
		}
		else
		{
			name = path;
		}
		return name;
	}
}
