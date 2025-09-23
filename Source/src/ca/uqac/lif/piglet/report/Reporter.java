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
	 * @param r The report data structure
	 * @throws ReporterException If an error occurs while writing the report
	 */
	public void report(FilePath root, Report r) throws ReporterException;
	
	/**
	 * An exception that can be thrown by a reporter. This class is expected
	 * to wrap any other exception that may occur during the reporting process.
	 */
	public static class ReporterException extends Throwable
	{
		/**
		 * Dummy UID 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new exception with the given message.
		 * @param message The message
		 */
		public ReporterException(String message)
		{
			super(message);
		}
		
		/**
		 * Creates a new exception with the given cause.
		 * @param t The cause
		 */
		public ReporterException(Throwable t)
		{
			super(t);
		}
	}
}
