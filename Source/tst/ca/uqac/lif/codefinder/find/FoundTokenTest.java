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
package ca.uqac.lif.codefinder.find;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.uqac.lif.azrael.PrintException;
import ca.uqac.lif.azrael.ReadException;
import ca.uqac.lif.azrael.xml.XmlPrinter;
import ca.uqac.lif.azrael.xml.XmlReader;
import ca.uqac.lif.xml.XmlElement;

public class FoundTokenTest
{
	@Test
	public void testSave() throws PrintException, ReadException
	{
		FoundToken ft = new FoundToken("assert", "MyFile.java", 10, 20, "assert x > 0;");
		XmlPrinter xp = new XmlPrinter();
		Object out = xp.print(ft);
		assertTrue(out instanceof XmlElement);
		XmlElement xe = (XmlElement) out;
		XmlReader xr = new XmlReader();
		Object o2 = xr.read(xe);
		assertTrue(o2 instanceof FoundToken);
		FoundToken ft2 = (FoundToken) o2;
		assertEquals(ft.getSnippet(), ft2.getSnippet());
	}
}
