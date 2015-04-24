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
	
	import sv.data.comparers.StringComparer;
	
	[TestFixture]
	public class StringComparerTests
	{
		private var stringComparer:StringComparer;
		private var stringComparerIgnoreCase:StringComparer;
		
		
		public function StringComparerTests()
		{
			
		}
		
		
		[SetUp]
		public function setup() : void
		{
			stringComparer = new StringComparer();
			stringComparerIgnoreCase = new StringComparer(true);
		}
		
		///////////////////////////////////////////
		// Compare Tests
		///////////////////////////////////////////
		
		[Test]
		public function compareHandleNulls() : void
		{
			var string1:String = "123456";
			
			Assert.areEqual( 0, stringComparer.compare(null, null) );
			Assert.areEqual( 0, stringComparerIgnoreCase.compare(null, null) );
			
			Assert.areEqual(-1, stringComparer.compare(null, string1) );
			Assert.areEqual(-1, stringComparerIgnoreCase.compare(null, string1) );
			
			Assert.areEqual( 1, stringComparer.compare(string1, null) );
			Assert.areEqual( 1, stringComparerIgnoreCase.compare(string1, null) );
		}
		
		[Test]
		public function compareHandleEmpty() : void
		{
			var string1:String = "123456";
			
			Assert.areEqual( 0, stringComparer.compare("", "") );
			Assert.areEqual( 0, stringComparerIgnoreCase.compare("", "") );
			
			Assert.areEqual(-1, stringComparer.compare("", string1) );
			Assert.areEqual(-1, stringComparerIgnoreCase.compare("", string1) );
			
			Assert.areEqual( 1, stringComparer.compare(string1, "") );
			Assert.areEqual( 1, stringComparerIgnoreCase.compare(string1, "") );
		}
		
		[Test]
		public function compareHandleAreSame() : void
		{
			var string1:String = "123456";
			
			Assert.areEqual( 0, stringComparer.compare(string1, string1) );
		}
		
		[Test]
		public function compare() : void
		{
			var string1:String = "123456";
			var string2:String = "123456";
			var string3:String = "654321";
			
			Assert.areEqual( 0, stringComparer.compare(string1, string2) );
			
			Assert.areEqual(-1, stringComparer.compare(string2, string3) );
			Assert.areEqual( 1, stringComparer.compare(string3, string2) );
		}
		
		[Test]
		[ExpectedError("ArgumentError")]
		public function compareFailureOnTypeMismatch() : void
		{
			var string1:String = "123456";
			var number:Number = 123456;
			
			stringComparer.compare(string1, number);
		}
		
		[Test]
		[ExpectedError("ArgumentError")]
		public function compareFailureOnTypeMismatch2() : void
		{
			var string1:String = "123456";
			var number:Number = 123456;
			
			stringComparer.compare(number, string1);
		}
		
		///////////////////////////////////////////
		// Equals Tests
		///////////////////////////////////////////
		
		[Test]
		public function equalsHandleNulls() : void
		{
			var string1:String = "123456";
			
			Assert.isTrue( stringComparer.equals(null, null) );
			
			Assert.isFalse( stringComparer.equals(null, string1) );
			Assert.isFalse( stringComparer.equals(string1, null) );
		}
		
		[Test]
		public function equalsHandleEmpty() : void
		{
			var string1:String = "123456";
			
			Assert.isTrue( stringComparer.equals("", "") );
			
			Assert.isFalse( stringComparer.equals("", string1) );
			Assert.isFalse( stringComparer.equals(string1, "") );
		}
		
		[Test]
		public function equalsHandleAreSame() : void
		{
			var string1:String = "123456";
			
			Assert.isTrue( stringComparer.equals(string1, string1) );
		}
		
		[Test]
		public function equals() : void
		{
			var string1:String = "123456";
			var string2:String = "123456";
			var string3:String = "654321";
			
			Assert.isTrue( stringComparer.equals(string1, string2) );
			Assert.isTrue( stringComparerIgnoreCase.equals(string1, string2) );
			
			Assert.isFalse( stringComparer.equals(string2, string3) );
			Assert.isFalse( stringComparer.equals(string3, string2) );
			Assert.isFalse( stringComparerIgnoreCase.equals(string2, string3) );
			Assert.isFalse( stringComparerIgnoreCase.equals(string3, string2) );
		}
		
		[Test]
		public function equalsIgnoringCase() : void
		{
			var string1:String = "Hello World!";
			var string2:String = "hello world!";
			var string3:String = "HELLO world!";
			var string4:String = "goodbye world!";
			
			Assert.isTrue( stringComparerIgnoreCase.equals(string1, string2) );
			Assert.isTrue( stringComparerIgnoreCase.equals(string1, string3) );
			
			Assert.isFalse( stringComparerIgnoreCase.equals(string1, string4) );
		}
		
		[Test]
		[ExpectedError("ArgumentError")]
		public function equalsFailureOnTypeMismatch() : void
		{
			var string1:String = "123456";
			var number:Number = 123456;
			
			stringComparer.equals(string1, number);
		}
		
		[Test]
		[ExpectedError("ArgumentError")]
		public function equalsFailureOnTypeMismatch2() : void
		{
			var string1:String = "123456";
			var number:Number = 123456;
			
			stringComparer.equals(number, string1);
		}
		
	}
	
}