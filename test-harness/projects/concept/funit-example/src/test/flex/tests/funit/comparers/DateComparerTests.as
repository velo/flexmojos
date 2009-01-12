/*	
	
	Copyright (c) 2007-2008 Ryan Christiansen
	
	This software is provided 'as-is', without any express or implied
	warranty. In no event will the authors be held liable for any damages
	arising from the use of this software.
	
	Permission is granted to anyone to use this software for any purpose,
	including commercial applications, and to alter it and redistribute it
	freely, subject to the following restrictions:
	
	1. The origin of this software must not be misrepresented; you must not
	claim that you wrote the original software. If you use this software
	in a product, an acknowledgment in the product documentation would be
	appreciated but is not required.
	
	2. Altered source versions must be plainly marked as such, and must not be
	misrepresented as being the original software.
	
	3. This notice may not be removed or altered from any source
	distribution.
	
*/
package tests.funit.comparers
{
	import funit.framework.Assert;
	
	import sv.data.comparers.DateComparer;
	
	[TestFixture]
	public class DateComparerTests
	{
		private var dateComparer:DateComparer;
		
		
		public function DateComparerTests()
		{
			
		}
		
		
		[SetUp]
		public function setup() : void
		{
			dateComparer = new DateComparer();
		}
		
		///////////////////////////////////////////
		// Compare Tests
		///////////////////////////////////////////
		
		[Test]
		public function compareHandleNulls() : void
		{
			var date1:Date = new Date(123456);
			
			Assert.areEqual( 0, dateComparer.compare(null, null) );
			
			Assert.areEqual(-1, dateComparer.compare(null, date1) );
			Assert.areEqual( 1, dateComparer.compare(date1, null) );
		}
		
		[Test]
		public function compareHandleAreSame() : void
		{
			var date1:Date = new Date(123456);
			
			Assert.areEqual( 0, dateComparer.compare(date1, date1) );
		}
		
		[Test]
		public function compare() : void
		{
			var date1:Date = new Date(123456);
			var date2:Date = new Date(123456);
			var date3:Date = new Date(654321);
			
			Assert.areEqual( 0, dateComparer.compare(date1, date2) );
			
			Assert.areEqual(-1, dateComparer.compare(date2, date3) );
			Assert.areEqual( 1, dateComparer.compare(date3, date2) );
		}
		
		[Test]
		[ExpectedError("ArgumentError")]
		public function compareFailureOnTypeMismatch() : void
		{
			var date1:Date = new Date(123456);
			var number:Number = 123456;
			
			dateComparer.compare(date1, number);
		}
		
		[Test]
		[ExpectedError("ArgumentError")]
		public function compareFailureOnTypeMismatch2() : void
		{
			var date1:Date = new Date(123456);
			var number:Number = 123456;
			
			dateComparer.compare(number, date1);
		}
		
		///////////////////////////////////////////
		// Equals Tests
		///////////////////////////////////////////
		
		[Test]
		public function equalsHandleNulls() : void
		{
			var date1:Date = new Date(123456);
			
			Assert.isTrue( dateComparer.equals(null, null) );
			
			Assert.isFalse( dateComparer.equals(null, date1) );
			Assert.isFalse( dateComparer.equals(date1, null) );
		}
		
		[Test]
		public function equalsHandleAreSame() : void
		{
			var date1:Date = new Date(123456);
			
			Assert.isTrue( dateComparer.equals(date1, date1) );
		}
		
		[Test]
		public function equals() : void
		{
			var date1:Date = new Date(123456);
			var date2:Date = new Date(123456);
			var date3:Date = new Date(654321);
			
			Assert.isTrue( dateComparer.equals(date1, date2) );
			
			Assert.isFalse( dateComparer.equals(date2, date3) );
			Assert.isFalse( dateComparer.equals(date3, date2) );
		}
		
		[Test]
		[ExpectedError("ArgumentError")]
		public function equalsFailureOnTypeMismatch() : void
		{
			var date1:Date = new Date(123456);
			var number:Number = 123456;
			
			dateComparer.equals(date1, number);
		}
		
		[Test]
		[ExpectedError("ArgumentError")]
		public function equalsFailureOnTypeMismatch2() : void
		{
			var date1:Date = new Date(123456);
			var number:Number = 123456;
			
			dateComparer.equals(number, date1);
		}
		
	}
	
}