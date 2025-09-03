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

import java.util.concurrent.locks.ReentrantLock;

import ca.uqac.lif.codefinder.Main;
import ca.uqac.lif.codefinder.util.AnsiPrinter.Color;

/**
 * A callback to report status of a long operation in the console.
 */
public class StatusCallback implements Runnable
{
	/** The width of the progress bar */
	protected static final int s_barWidth = 32;
	
	/** The number of items currently done */
	protected int m_currentlyDone;
	
	/** A lock to protect access to the currently done counter */
	protected final ReentrantLock m_lock;
	
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
		m_lock = new ReentrantLock();
		printBar();
	}
	
	@Override
	public void run()
	{
		m_startTime = System.currentTimeMillis();
		printBar();
		while (m_currentlyDone < m_total)
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
	
	/**
	 * Marks one item as done and updates the display if needed.
	 */
	public void done()
	{
		m_lock.lock();
		m_currentlyDone++;
		m_lock.unlock();
	}	
	
	/**
	 * Prints the progress bar.
	 */
	protected void printBar()
	{
		m_lock.lock();
		int done = m_currentlyDone;
		m_lock.unlock();
		m_out.print("\r\033[2K");
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
		long eta = calculateEta(done);
		if (eta >= 0)
		{
			m_out.print(String.format(" ETA: %s", Main.formatDuration(eta)));
		}
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