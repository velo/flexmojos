/**
 * Flexmojos is a set of maven goals to allow maven users to compile,
 * optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
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