/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sonatype.flexmojos.coverage;

import apparat.tools.coverage.CoverageConfiguration;
import org.sonatype.flexmojos.util.PathUtil;
import scala.collection.immutable.List;
import scala.collection.mutable.ListBuffer;

import java.io.File;

/**
 * @author Joa Ebert
 */
final class CoverageConfigurationImpl implements CoverageConfiguration
{
	private final ListBuffer<String> _sourcePath = new ListBuffer<String>();
	private final File _input;
	private final File _output;

	public CoverageConfigurationImpl(final File input, final File output, final File... sourcePath)
	{
		_input = input;
		_output = output;

		for(final File sourcePathElement : sourcePath)
		{
			//
			// Java equivalent of the following Scala code:
			// _sourcePath += PathUtil getCanonicalPath sourcePathElement
			//

			_sourcePath.$plus$eq(PathUtil.path( sourcePathElement));
		}
	}

	public File input()
	{
		return _input;
	}

	public File output()
	{
		return _output;
	}

	public List<String> sourcePath()
	{
		//
		// Convert the mutable ListBuffer[String] to an immutable List[String] for Apparat.
		//
		
		return _sourcePath.toList();
	}
}
