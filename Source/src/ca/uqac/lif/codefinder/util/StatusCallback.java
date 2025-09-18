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
package ca.uqac.lif.codefinder.util;

import java.util.concurrent.atomic.AtomicInteger;

import ca.uqac.lif.codefinder.util.AnsiPrinter.Color;

/**
 * A callback to report status of a long operation in the console.
 */
public class StatusCallback implements Runnable
{
	/** The width of the progress bar */
	protected static final int s_barWidth = 32;
	
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
	
	/**
	 * Creates a new status callback.
	 * @param out The output printer
	 * @param total The total number of items to process
	 */
	public StatusCallback(AnsiPrinter out, int total)
	{
		super();
		m_out = out;
		m_total = total;
		m_currentlyDone = new AtomicInteger(0);
		m_resolutionTimeouts = new AtomicInteger(0);
		printBar();
	}
	
	@Override
	public void run()
	{
		m_startTime = System.currentTimeMillis();
		printBar();
		while (m_currentlyDone.get() < m_total)
		{
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				// Ignore
			}
			printBar();
		}
	}
	
	public void cleanup()
	{
		m_out.moveBeginningLine().clearLine().moveStartLastLine().clearLine();
	}
	
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
	 * Prints the progress bar.
	 */
	protected void printBar()
	{
		int timeouts = m_resolutionTimeouts.get();
		m_out.clearLine();
		m_out.println("Timeouts: " + String.format("%3d", timeouts) + " ");
		int done = m_currentlyDone.get();
		m_out.clearLine();
		m_out.setForegroundColor(Color.LIGHT_GRAY);
		m_out.print("[");
		m_out.setForegroundColor(Color.RED);
		int chars = (int) Math.ceil(((float) done / (float) m_total) * s_barWidth);
		for (int i = 0; i < chars; i++)
		{
			m_out.print("#");
		}
		for (int i = chars; i < s_barWidth; i++)
		{
			m_out.print(" ");
		}
		m_out.setForegroundColor(Color.LIGHT_GRAY);
		m_out.print("] ");
		m_out.resetColors();
		int width = Integer.toString(m_total).length();
		m_out.print(String.format("%" + width + "d/%" + width + "d", done, m_total));
		long elapsed = System.currentTimeMillis() - m_startTime;
		m_out.print(String.format(" Elapsed: %s", AnsiPrinter.formatHms(elapsed)));
		long eta = calculateEta(done);
		if (eta >= 0)
		{
			m_out.print(String.format(" ETA: %s", AnsiPrinter.formatDuration(eta)));
		}
		m_out.moveStartLastLine();
	}
	
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