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
package ca.uqac.lif.piglet.find;

import java.util.ArrayList;
import java.util.List;

import ca.uqac.lif.azrael.ObjectPrinter;
import ca.uqac.lif.azrael.ObjectReader;
import ca.uqac.lif.azrael.PrintException;
import ca.uqac.lif.azrael.Printable;
import ca.uqac.lif.azrael.ReadException;
import ca.uqac.lif.azrael.Readable;

public class FactoryCache implements Readable, Printable
{
	protected final List<FoundToken> m_tokens;

	protected final long m_expected;

	protected final long m_finished;

	protected final String m_hash;

	protected final String m_name;

	/**
	 * Used only for deserialization.
	 */
	protected FactoryCache()
	{
		this(null, null, null, 0, 0);
	}

	public FactoryCache(String name, List<?> found, String hash, long expected, long finished)
	{
		super();
		m_name = name;
		m_tokens = new ArrayList<FoundToken>();
		if (found != null)
		{
			for (Object o : found)
			{
				if (o instanceof FoundToken)
				{
					m_tokens.add((FoundToken) o);
				}
			}
		}
		m_expected = expected;
		m_finished = finished;
		m_hash = hash;
	}

	public long getExpected()
	{
		return m_expected;
	}

	public long getFinished()
	{
		return m_finished;
	}

	public List<FoundToken> getFoundTokens()
	{
		return m_tokens;
	}

	public String getName()
	{
		return m_name;
	}

	@Override
	public Object print(ObjectPrinter<?> printer) throws PrintException
	{
		List<Object> list = new ArrayList<>();
		list.add(m_name);
		list.add(m_hash);
		list.add(m_finished);
		list.add(m_expected);
		list.add(m_tokens);
		return printer.print(list);
	}

	@Override
	public Object read(ObjectReader<?> reader, Object o) throws ReadException
	{
		Object o_list = reader.read(o);
		if (!(o_list instanceof List))
		{
			throw new ReadException("Expected a list");
		}
		List<?> list = (List<?>) o_list;
		Object o_name = list.get(0);
		if (!(o_name instanceof String))
		{
			throw new ReadException("Expected a string");
		}
		Object o_hash = list.get(1);
		if (!(o_hash instanceof String))
		{
			throw new ReadException("Expected a string");
		}
		Object o_expected = list.get(2);
		if (!(o_expected instanceof Long))
		{
			throw new ReadException("Expected a long");
		}
		Object o_finished = list.get(3);
		if (!(o_finished instanceof Long))
		{
			throw new ReadException("Expected a long");
		}
		Object o_tokens = list.get(4);
		if (!(o_tokens instanceof List))
		{
			throw new ReadException("Expected a list");
		}
		return new FactoryCache((String) o_name, (List<?>) o_tokens,
				(String) o_hash, (Long) o_finished, (Long) o_expected);
	}
}