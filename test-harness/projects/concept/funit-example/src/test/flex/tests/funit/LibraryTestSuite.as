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