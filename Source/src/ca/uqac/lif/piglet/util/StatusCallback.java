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

import java.util.concurrent.atomic.AtomicInteger;

import ca.uqac.lif.util.AnsiPrinter;

/**
 * A callback to report status of a long operation.
 */
public abstract class StatusCallback implements Runnable
{
	/** The number of items currently done */
	protected final AtomicInteger m_currentlyDone;

	/** The number of resolution timeouts encountered so far */
	protected final AtomicInteger m_resolutionTimeouts;
	
	/** The total number of items */
	protected final int m_total;
	
	/** The output printer */
	protected final AnsiPrinter m_out;
	
	/** The start time of the operation */
	protected long m_startTime = 0;
	
	/** The current project being processed */
	protected String m_currentProject = "";
	
	/** The number of threads used */
	protected final int m_numThreads;
	
	/** An optional filter condition */
	protected final String m_filterCondition;
	
	/**
	 * Creates a new status callback.
	 * @param out The output printer
	 * @param total The total number of items to process
	 */
	public StatusCallback(AnsiPrinter out, int total, int threads, String filter_condition)
	{
		super();
		m_out = out;
		m_total = total;
		m_currentlyDone = new AtomicInteger(0);
		m_resolutionTimeouts = new AtomicInteger(0);
		m_numThreads = threads;
		m_filterCondition = filter_condition;
	}
	
	public abstract void cleanup();
	
	/**
	 * Marks one item as done and updates the display if needed.
	 */
	public void done()
	{
		m_currentlyDone.incrementAndGet();
	}	
	
	/**
	 * Marks that a resolution timeout has occurred.
	 */
	public void resolutionTimeout()
	{
		m_resolutionTimeouts.incrementAndGet();
	}
	
	/**
	 * Sets the name of the current project being processed.
	 * @param project The name of the project
	 */
	public void setCurrentProject(String project)
	{
		m_currentProject = project;
	}
	
	/**
	 * Reports an error message.
	 * @param msg The error message
	 */
	public abstract void error(String msg);


	/**
	 * Calculates the estimated time of arrival (ETA) in milliseconds.
	 * @param done The number of items done so far
	 * @return The ETA in milliseconds, or -1 if it cannot be calculated
	 */
	protected long calculateEta(int done)
	{
		if (done == 0)
		{
			return -1;
		}
		long now = System.currentTimeMillis();
		long elapsed = now - m_startTime;
		double avg_time_per_item = (double) elapsed / (double) done;
		return (long) (avg_time_per_item * (m_total - done));
	}
	
	
}
