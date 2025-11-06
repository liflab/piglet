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
import ca.uqac.lif.util.AnsiPrinter.Color;

/**
 * A callback to report status of a long operation in the console
 * using ANSI escape codes.
 */
public class AnsiCallback extends StatusCallback
{
	/** The width of the progress bar */
	protected static final int s_barWidth = 32;
	
	/**
	 * Creates a new status callback.
	 * @param out The output printer
	 * @param total The total number of items to process
	 */
	public AnsiCallback(AnsiPrinter out, int total, int threads, String filter_condition)
	{
		super(out, total, threads, filter_condition);
		printBar();
	}
	
	@Override
	public void error(String msg)
	{
		m_out.moveBeginningLine().clearLine();
		m_out.setForegroundColor(Color.RED);
		m_out.println("Error: " + msg);
		m_out.resetColors();
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
	
	@Override
	public void cleanup()
	{
		m_out.moveBeginningLine().clearLine().moveStartLastLine().clearLine();
	}
	

	
	/**
	 * Prints the progress bar.
	 */
	protected void printBar()
	{
		int timeouts = m_resolutionTimeouts.get();
		m_out.clearLine();
		m_out.print("Project: ");
		m_out.fg(Color.LIGHT_PURPLE);
		m_out.print(AnsiPrinter.padToLength(m_currentProject, 16, true) + " ");
		m_out.resetColors();
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
	

}