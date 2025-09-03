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
package ca.uqac.lif.codefinder.assertion;

import ca.uqac.lif.codefinder.thread.ThreadContext;

public abstract class OptionalAssertionFinder extends AssertionFinder
{
	public OptionalAssertionFinder(String name, String filename)
	{
		super(name, filename);
	}
	
	protected OptionalAssertionFinder(String name, String filename, ThreadContext context)
	{
		super(name, filename, context);
	}

}
