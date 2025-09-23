package ca.uqac.lif.piglet.util;

import java.util.IdentityHashMap;

import ca.uqac.lif.piglet.Analysis;

public class MultiplexPrinter
{
	protected final IdentityHashMap<Analysis,AnsiPrinter> m_stdinPrinters;
	
	protected final IdentityHashMap<Analysis,AnsiPrinter> m_stderrPrinters;
	
	public MultiplexPrinter()
	{
		super();
		m_stdinPrinters = new IdentityHashMap<Analysis,AnsiPrinter>();
		m_stderrPrinters = new IdentityHashMap<Analysis,AnsiPrinter>();
	}
	
	public AnsiPrinter getStdinPrinter(Analysis a)
	{
		if (!m_stdinPrinters.containsKey(a))
		{
			m_stdinPrinters.put(a, new AnsiPrinter(System.out));
		}
		return m_stdinPrinters.get(a);
	}
	
	public AnsiPrinter getStderrPrinter(Analysis a)
	{
		if (!m_stderrPrinters.containsKey(a))
		{
			m_stderrPrinters.put(a, new AnsiPrinter(System.err));
		}
		return m_stderrPrinters.get(a);
	}
}
