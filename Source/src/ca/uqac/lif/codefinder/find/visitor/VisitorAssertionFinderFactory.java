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
package ca.uqac.lif.codefinder.find.visitor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bsh.EvalError;
import bsh.Interpreter;
import ca.uqac.lif.codefinder.Main;
import ca.uqac.lif.codefinder.find.TokenFinderFactory;
import ca.uqac.lif.codefinder.util.Paths;
import ca.uqac.lif.fs.FileSystem;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.FileUtils;

public class VisitorAssertionFinderFactory extends TokenFinderFactory
{
	/**
	 * Pattern to extract the name of an assertion from a comment 
	 */
	protected static final Pattern s_namePat = Pattern.compile("Name:([^\\*]+)");
	
	/**
	 * Creates a new visitor-based token finder factory.
	 * 
	 * @param name
	 *          The name of this finder
	 */
	public VisitorAssertionFinderFactory(String name)
	{
		super(name);
	}

	@Override
	public VisitorAssertionFinder newFinder()
	{
		// To be implemented by subclasses
		return null;
	}

	/**
	 * Reads a BeanShell script from a file and instantiates a
	 * @param hd
	 * @param filename
	 * @return
	 * @throws FileSystemException
	 * @throws EvalError
	 */
	public static VisitorAssertionFinderFactory readBeanshell(FileSystem hd, String filename) throws TokenFinderFactoryException
	{
		try
		{
			String bsh_code = FileUtils.readStringFrom(hd, Paths.getFilename(filename));
			bsh_code.replaceAll("^\\s*void visit\\(", "public void visit(");
			bsh_code.replaceAll("^\\s*void leave\\(", "public void leave(");
			Interpreter interpreter = new Interpreter();
			// Use the same loader that sees your app's classes
			ClassLoader appCl = Main.class.getClassLoader();
			interpreter.setClassLoader(appCl);
			Thread.currentThread().setContextClassLoader(appCl);
			StringBuilder code = new StringBuilder();
			Matcher mat = s_namePat.matcher(bsh_code);
			String name = "Unnamed AST finder";
			if (mat.find())
			{
				name = mat.group(1).trim();
			}
			String head = new String(
					FileUtils.toBytes(VisitorAssertionFinder.class.getResourceAsStream("top.bsh")));
			head = head.replace("$NAME$", name);
			code.append(head);
			code.append(bsh_code);

			code.append(new String(
					FileUtils.toBytes(VisitorAssertionFinder.class.getResourceAsStream("bottom.bsh"))));
			Object o = interpreter.eval(code.toString());
			if (o == null || !(o instanceof VisitorAssertionFinderFactory))
			{
				return null;
			}
			return (VisitorAssertionFinderFactory) o;
		}
		catch (FileSystemException | EvalError e)
		{
			throw new TokenFinderFactoryException(e);
		}
	}
}