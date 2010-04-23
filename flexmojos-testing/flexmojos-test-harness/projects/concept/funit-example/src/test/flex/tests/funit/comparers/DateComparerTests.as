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