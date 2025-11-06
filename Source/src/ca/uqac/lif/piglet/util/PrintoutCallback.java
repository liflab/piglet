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

import ca.uqac.lif.util.AnsiPrinter;

/**
 * A status callback that prints progress information as plain text.
 * This is useful when the output is being redirected to a file, as
 * it does not rely on ANSI escape codes and sends updates at a
 * slower rate than the {@link AnsiCallback} (10 seconds instead of
 * 1 second).
 */
public class PrintoutCallback extends StatusCallback
{
	/**
	 * Creates a new printout callback.
	 * @param out The output printer
	 * @param total The total number of items to process
	 */
	public PrintoutCallback(AnsiPrinter out, int total, int threads)
	{
		super(out, total, threads);
		m_out.disableColors();
	}
	
	@Override
	public void error(String msg)
	{
		m_out.println("Error: " + msg);
	}
	
	@Override
	public void run()
	{
		m_startTime = System.currentTimeMillis();
		m_out.println("Project: " + m_currentProject);
		m_out.println("Starting processing of " + m_total + " items... (" + m_numThreads + " threads)");
		printBar();
		while (m_currentlyDone.get() < m_total)
		{
			try
			{
				Thread.sleep(10000);
			}
			catch (InterruptedException e)
			{
				// Ignore
			}
			printBar();
		}
	}
	
	@Override
	public void cleanup()
	{
		// Do nothing
	}
	
	/**
	 * Prints the progress bar.
	 */
	protected void printBar()
	{
		int timeouts = m_resolutionTimeouts.get();
		m_out.println("Timeouts: " + String.format("%3d", timeouts) + " ");
		int done = m_currentlyDone.get();
		int width = Integer.toString(m_total).length();
		m_out.print(String.format("%" + width + "d/%" + width + "d", done, m_total));
		long elapsed = System.currentTimeMillis() - m_startTime;
		m_out.print(String.format(" Elapsed: %s", AnsiPrinter.formatHms(elapsed)));
		long eta = calculateEta(done);
		if (eta >= 0)
		{
			m_out.print(String.format(" ETA: %s", AnsiPrinter.formatDuration(eta)));
		}
		m_out.println();
	}
}