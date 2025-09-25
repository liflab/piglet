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
package ca.uqac.lif.util;

/**
 * A clock that allows time to be stopped and manipulated. One can consider
 * this class as a simulation of an analog wall clock with an on/off switch.
 * @author Sylvain Hallé
 */
public class WallClock implements Clock
{
	/**
	 * The local system time considered as the start time of the clock.
	 */
	protected long m_startTime;
	
	/**
	 * The local system time corresponding to the last moment the clock was
	 * asked to output its current time. 
	 */
	protected long m_lapTime;
	
	/**
	 * The current time displayed by the clock the last time it was asked.
	 */
	protected long m_currentTime;
	
	/**
	 * A flag indicating if the clock is currently stopped.
	 */
	protected boolean m_stopped;
	
	/**
	 * The speed at which the clock is moving. A factor of 1 indicates that 1
	 * second of "real-world" elapsed time corresponds to 1 second in the clock.
	 */
	protected double m_factor;
	
	/**
	 * Creates a new clock set at an arbitrary time. The clock is not
	 * automatically started.
	 * @param start_time The time in milliseconds
	 */
	public WallClock(long start_time)
	{
		super();
		m_startTime = start_time;
		m_lapTime = start_time;
		m_currentTime = start_time;
		m_stopped = true;
		m_factor = 1;
	}
	
	/**
	 * Creates a new clock set at the current system time. The clock is not
	 * automatically started.
	 */
	public WallClock()
	{
		this(getCurrentRealTime());
	}
	
	/**
	 * Resets the clock to the time it was displaying when initialized. This
	 * method does not change the running status (started or stopped) of the
	 * clock.
	 * @return This clock
	 */
	/*@ non_null @*/ synchronized public WallClock reset()
	{
		m_lapTime = getCurrentRealTime();
		m_currentTime = m_startTime;
		return this;
	}
	
	/**
	 * Synchronizes the time of this clock with the current system time. This
	 * method does not change the running status (started or stopped) of the
	 * clock.
	 * @return This clock
	 */
	/*@ non_null @*/ synchronized public WallClock synchronize()
	{
		setCurrentTime(getCurrentRealTime());
		return this;
	}
	
	/**
	 * Get the current time displayed by the clock. This
	 * method does not change the running status (started or stopped) of the
	 * clock. The algorithm to display the current time is as follows:
	 * <ul>
	 * <li>If the clock is currently stopped, return the last displayed
	 * time.</li>
	 * <li>If the clock is running:
	 *   <ul>
	 *   <li>measure the elapsed time between the current system time and the
	 *   last time the clock was asked (the "lap" time)</li>
	 *   <li>add this interval to the clock's current time</li>
	 *   <li>set the lap time to the system's current time</li>
	 *   <li>return the clock's current time</li>
	 * </ul>
	 * @return The time in milliseconds
	 */
	@Override
	synchronized public long getCurrentTimeMillis()
	{
		if (!m_stopped)
		{
			long current = getCurrentRealTime();
			long elapsed = current - m_lapTime;
			m_currentTime += ((double) elapsed) * m_factor;
			m_lapTime = current;
		}
		return m_currentTime;
	}
	
	/**
	 * Sets the clock's current time. The method does not change the running
	 * status of the clock.
	 * @param time_ms The time in milliseconds
	 * @return This clock
	 */
	/*@ non_null @*/ synchronized public WallClock setCurrentTime(long time_ms)
	{
		m_currentTime = time_ms;
		return this;
	}
	
	/**
	 * Stops the clock. The clock will keep the time it is displaying at the
	 * moment the method is called until it is restarted.
	 * @return This clock
	 */
	/*@ non_null @*/ synchronized public WallClock stop()
	{
		if (!m_stopped)
		{
			setRunningState(false);
		}
		return this;
	}
	
	/**
	 * Starts the clock. The clock will resume incrementing time from the time
	 * it was displaying when the method was called.
	 * @return This clock
	 */
	/*@ non_null @*/ synchronized public WallClock start()
	{
		if (m_stopped)
		{
			setRunningState(true);
		}
		return this;
	}
	
	/**
	 * Gets the offset between the current system time and the time currently
	 * displayed by the clock.
	 * @return A difference in milliseconds; a positive value indicates that the
	 * clock is behind the current system time, while a negative value indicates
	 * the opposite
	 */
	synchronized public long getOffset()
	{
		return getCurrentRealTime() - getCurrentTimeMillis();
	}
	
	/*@ non_null @*/ synchronized public WallClock setFactor(double factor)
	{
		m_factor = factor;
		return this;
	}
	
	/*@ non_null @*/ synchronized public WallClock advanceBy(long interval_ms)
	{
		long current = getCurrentTimeMillis();
		setCurrentTime(current + interval_ms);
		return this;
	}
	
	/**
	 * Sets the running state of the clock.
	 * @param runs Set to <tt>true</tt> to start the clock, <tt>false</tt>
	 * to stop it
	 */
	protected void setRunningState(boolean runs)
	{
		if (!runs)
		{
			m_stopped = true;
			getCurrentTimeMillis();
		}
		else
		{
			m_stopped = false;
			m_lapTime = getCurrentRealTime();
		}
	}
	
	/**
	 * Gets the current local system time.
	 * @return The time in milliseconds
	 */
	protected static long getCurrentRealTime()
	{
		return System.currentTimeMillis();
	}
	
	/**
	 * Exposes a read-only access to the current time of another clock. The main
	 * purpose of this class is to encapsulate a wall clock, controlled by some
	 * process, to other objects that should only query the current time without
	 * being able to modify it.
	 * 
	 * @author Sylvain Hallé
	 */
	public static class ReadOnlyClock implements Clock
	{
		/**
		 * The underlying clock providing the current time.
		 */
		/*@ non_null @*/ protected final Clock m_clock;
		
		/**
		 * Creates a new read-only clock.
		 * @param c The underlying clock providing the current time
		 */
		public ReadOnlyClock(/*@ non_null @*/ Clock c)
		{
			super();
			m_clock = c;
		}

		@Override
		public long getCurrentTimeMillis()
		{
			return m_clock.getCurrentTimeMillis();
		}
	}
}
