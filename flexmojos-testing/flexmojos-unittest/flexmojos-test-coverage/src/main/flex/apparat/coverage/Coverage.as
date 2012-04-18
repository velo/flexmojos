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
package apparat.coverage
{
	import net.flexmojos.oss.coverage.CoverageDataCollector;
	import net.flexmojos.oss.test.report.TestCoverageReport;
	import mx.collections.ArrayCollection;

	public class Coverage
	{

		private static var cache:Object=new Object();

		public static function onSample(file:String, line:int):void
		{
			if (cache[file] == null)
			{
				cache[file]=new ArrayCollection();
				ArrayCollection(cache[file]).addItem(line);
			}
			else
			{
				if (ArrayCollection(cache[file]).contains(line))
				{
					return;
				}
				else
				{
					ArrayCollection(cache[file]).addItem(line);
				}
			}

			CoverageDataCollector.collect(file, line);
		}
	}
}
