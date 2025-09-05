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
package ca.uqac.lif.codefinder.thread;

import com.github.javaparser.JavaParser;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;

/**
 * A context specific to a single thread, containing objects that
 * are not thread-safe and need to be duplicated for each thread.
 * In the case of JavaParser, this includes the type solver,
 * the parser and the parser facade.
 */
public final class ThreadContext
{
	/**
	 * The type solver
	 */
	final CombinedTypeSolver ts;
	
	/**
	 * A Java parser instance
	 */
	final JavaParser parser;
	
	/**
	 * A Java parser facade
	 */
	final JavaParserFacade facade;
	
	/**
	 * A timeout for type resolution operations (in milliseconds).
	 */
	final long m_resolutionTimeout;

	/**
	 * Creates a new thread context.
	 * @param ts The type solver
	 * @param parser A Java parser instance
	 * @param facade A Java parser facade
	 * @param resolutionTimeout A timeout for type resolution operations (in milliseconds)
	 */
	public ThreadContext(CombinedTypeSolver ts, JavaParser parser, JavaParserFacade facade, long resolutionTimeout)
	{
		this.ts = ts;
		this.parser = parser;
		this.facade = facade;
		m_resolutionTimeout = resolutionTimeout;
	}
	
	/**
	 * Gets the type solver instance specific to this thread. Some token
	 * finders need it to resolve types.
	 * @return The type solver instance
	 */
	public CombinedTypeSolver getTypeSolver()
	{
		return ts;
	}
	
	/**
	 * Gets the resolution timeout in milliseconds.
	 * @return The resolution timeout in milliseconds
	 */
	public long getResolutionTimeout()
	{
		return m_resolutionTimeout;
	}
	
	/**
	 * Gets the Java parser instance specific to this thread.
	 * @return The Java parser instance
	 */
	public JavaParser getParser()
	{
		return parser;
	}
	
}