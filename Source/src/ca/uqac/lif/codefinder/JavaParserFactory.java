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
package ca.uqac.lif.codefinder;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

/**
 * A class that provides a JavaParser configuration with symbol resolution
 * capabilities.
 */
public class JavaParserFactory
{
	/**
	 * Returns a JavaParser configuration that can resolve symbols using the
	 * given source paths.
	 * @param source_paths An array of source paths to be used for symbol
	 * resolution
	 * @return A parser configuration with symbol resolution capabilities
	 */
	public static ParserConfiguration getConfiguration(String[] source_paths)
	{
		CombinedTypeSolver typeSolver = new CombinedTypeSolver();
		typeSolver.add(new ReflectionTypeSolver());
		for (String s_sourcePath : source_paths)
		{
			typeSolver.add(new JavaParserTypeSolver(s_sourcePath));
		}
		ParserConfiguration parserConfiguration =
				new ParserConfiguration().setSymbolResolver(
						new JavaSymbolSolver(typeSolver));
		return parserConfiguration;
	}
}
