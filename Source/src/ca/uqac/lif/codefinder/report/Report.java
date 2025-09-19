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
package ca.uqac.lif.codefinder.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A data structure representing a report of the results of a code
 * search.
 */
public abstract class Report
{
	public static final String PATH_SEPARATOR = "/";

	protected Report()
	{
		super();
	}

	public static class ObjectReport extends Report
	{
		public final Object m_object;

		public ObjectReport(Object o)
		{
			super();
			m_object = o;
		}

		@Override
		public String toString()
		{
			return m_object.toString();
		}

		public Object getObject()
		{
			return m_object;
		}
	}

	/**
	 * A report mapping strings to other reports.
	 */
	public static class MapReport extends Report
	{
		/**
		 * The map
		 */
		public final Map<String,Report> m_map;

		/**
		 * Creates a new empty map report.
		 */
		public MapReport()
		{
			this(new HashMap<String,Report>());
		}

		/**
		 * Creates a new map report with the given map.
		 * @param map
		 */
		protected MapReport(Map<String,Report> map)
		{
			super();
			m_map = map;
		}

		@Override
		public String toString()
		{
			return m_map.toString();
		}

		public Set<String> keySet()
		{
			return m_map.keySet();
		}

		public Report get(String key)
		{
			return m_map.get(key);
		}

		public void put(String key, Report value)
		{
			put(key.split(PATH_SEPARATOR), value, false);
		}

		public void append(String key, Object value)
		{
			put(key.split(PATH_SEPARATOR), value, true);
		}

		protected void put(String[] parts, Object value, boolean append)
		{
			if (parts.length > 1)
			{
				if (m_map.containsKey(parts[0]))
				{
					Report r = m_map.get(parts[0]);
					if (!(r instanceof MapReport))
					{
						throw new IllegalArgumentException("Cannot insert a report at key " + parts[0] + " because it is already occupied by a non-map report");
					}
					((MapReport) r).put(Arrays.copyOfRange(parts, 1, parts.length), value, append);
				}
				else
				{
					MapReport r = new MapReport();
					m_map.put(parts[0], r);
					r.put(Arrays.copyOfRange(parts, 1, parts.length), value, append);
				}
				return;
			}
			// We are at the end of the path
			if (!append)
			{
				if (value instanceof Report)
				{
					m_map.put(parts[0], (Report) value);
				}
				else
				{
					m_map.put(parts[0], new ObjectReport(value));
				}
				return;
			}
			if (append)
			{
				if (!m_map.containsKey(parts[0]))
				{
					List<Object> new_list = new ArrayList<Object>();
					new_list.add(value);
					ObjectReport or = new ObjectReport(new_list);
					m_map.put(parts[0], or);
				}
				else
				{
					Report in_r = m_map.get(parts[0]);
					if (!(in_r instanceof ObjectReport))
					{
						throw new IllegalArgumentException("Cannot append at the end of the path");
					}
					ObjectReport or = (ObjectReport) in_r;
					Object in_o = or.getObject();
					if (!(in_o instanceof Collection))
					{
						throw new IllegalArgumentException("Cannot append to a non-collection");
					}
					@SuppressWarnings("unchecked")
					Collection<Object> col = (Collection<Object>) in_o;
					col.add(value);
				}
			}
		}

		public Set<Map.Entry<String,Report>> entrySet()
		{
			return m_map.entrySet();
		}
	}
}
