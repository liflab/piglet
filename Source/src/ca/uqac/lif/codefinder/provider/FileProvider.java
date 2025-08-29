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
package ca.uqac.lif.codefinder.provider;

/**
 * An interface for objects that provide source files to be analyzed.
 * Implementations of this interface typically read files from a file system,
 * a compressed archive, or a version control repository.
 * 
 * @author Sylvain Hallé
 */
public interface FileProvider
{
	/**
	 * Indicates whether there are more files to be provided.
	 * @return <tt>true</tt> if there are more files, <tt>false</tt> otherwise
	 */
	public boolean hasNext();
	
	/**
	 * Provides the next file.
	 * @return The next file, or <tt>null</tt> if there are no more files
	 */
	public FileSource next();
	
	/**
	 * Returns the number of files that have been provided so far.
	 * @return The number of files provided
	 */
	public int filesProvided();
}
