/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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