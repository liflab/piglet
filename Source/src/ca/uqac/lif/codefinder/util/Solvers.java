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

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.*;

import ca.uqac.lif.fs.FileUtils;
import ca.uqac.lif.fs.HardDisk;

import java.nio.file.*;
import java.util.List;
import java.util.Set;

public final class Solvers
{
	private Solvers()
	{
		// Do nothing
		super();
	}

	/**
	 * Build a CombinedTypeSolver with JDK types, given source roots and jars.
	 * 
	 * @param sourceRoots
	 *          set of directories containing Java sources
	 * @param jarPaths
	 *          set of jar files (absolute or relative)
	 */
	@SuppressWarnings("deprecation")
	public static CombinedTypeSolver buildSolver(Set<String> sourceRoots, Set<String> jarPaths)
			throws Exception
	{
		CombinedTypeSolver ts = new CombinedTypeSolver();

		// JDK reflection
		ts.add(new ReflectionTypeSolver());

		// add sources
		for (String src : sourceRoots)
		{
			Path p = Paths.get(src).toAbsolutePath().normalize();
			if (!Files.isDirectory(p))
				throw new IllegalArgumentException("Not a directory: " + p);
			ts.add(new JavaParserTypeSolver(p.toFile()));
		}

		// add jars
		for (String jar : jarPaths)
		{
			Path p = Paths.get(jar).toAbsolutePath().normalize();
			if (Files.isDirectory(p))
			{
				HardDisk hd = new HardDisk(p.toString());
				List<String> contents = FileUtils.ls(hd, "", ".*jar$");
				for (String fn : contents)
				{
					ts.add(JarTypeSolver.getJarTypeSolver(p.toString() + "/" + fn));
				}
			}
			else
			{
				if (!Files.isRegularFile(p))
					throw new IllegalArgumentException("Not a jar file: " + p);
				ts.add(JarTypeSolver.getJarTypeSolver(p.toString()));
			}
		}
		return ts;
	}

	/** Create a parser configuration with this solver. */
	public static ParserConfiguration parserConfig(TypeSolver ts)
	{
		return new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_11)
				.setSymbolResolver(new JavaSymbolSolver(ts));
	}
}
