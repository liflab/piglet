package ca.uqac.lif.codefinder.provider;

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