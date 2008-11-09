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
package tests.funit
{
	import funit.core.TestSuite;
	
	import tests.funit.attributes.*;
	import tests.funit.comparers.*;
	import tests.funit.utils.*;
	
	public class LibraryTestSuite extends TestSuite
	{
		
		///////////////////////////////////////////
		// Singleton
		///////////////////////////////////////////
		
		private static var _instance:LibraryTestSuite = null;
		
		// get singleton instance
		public static function getInstance() : LibraryTestSuite
		{
			if (_instance == null)
			{
				_instance = new LibraryTestSuite();
			}
			
			return _instance;
		}
		
		///////////////////////////////////////////
		// Constructor
		///////////////////////////////////////////
		
		public function LibraryTestSuite()
		{
			init();
		}
		
		public function init() : void
		{
			// Data Comparer Test Cases
			this.add( DateComparerTests );
			this.add( NumericComparerTests );
			this.add( StringComparerTests );
			
			// Utility Test Cases
			this.add( GetClassTests );
			this.add( GetBaseClassTests );
			this.add( GetClassByNameTests );
			
			this.add( GetSimpleClassNameTests );
			this.add( GetSimpleBaseClassNameTests );
			
			this.add( GetQualifiedClassNameTests );
			this.add( GetQualifiedBaseClassNameTests );
			
			this.add( DescribeTypeTests );
		}
		
	}
	
}