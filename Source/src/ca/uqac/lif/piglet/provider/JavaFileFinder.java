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
package ca.uqac.lif.piglet.provider;

import java.util.ArrayList;
import java.util.List;

import ca.uqac.lif.fs.FilePath;
import ca.uqac.lif.fs.FileSystem;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.RecursiveListing;

public class JavaFileFinder extends RecursiveListing
	{
		protected final List<String> m_files;
		
		public JavaFileFinder(FileSystem fs)
		{
			super(fs);
			m_files = new ArrayList<String>();
		}
		
		public List<String> getFiles()
		{
			return m_files;
		}

		@Override
		protected void visit(FilePath fp) throws FileSystemException
		{
			String name = fp.toString();
			if (name.endsWith(".java"))
			{
				m_files.add(name);
			}
		}
		
		@Override
		protected void crawl(FilePath path) throws FileSystemException
		{
			// Don't explore hidden folders
			if (!path.toString().startsWith("."))
			{
				super.crawl(path);
			}
		}
	}