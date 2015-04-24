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
	
	import sv.data.comparers.NumericComparer;
	
	[TestFixture]
	public class NumericComparerTests
	{
		private var numericComparer:NumericComparer;
		
		
		public function NumericComparerTests()
		{
			
		}
		
		
		[SetUp]
		public function setup() : void
		{
			numericComparer = new NumericComparer();
		}
		
		///////////////////////////////////////////
		// Compare Tests
		///////////////////////////////////////////
		
		[Test]
		public function compareHandleNulls() : void
		{
			var number1:Number = 123.456;
			
			Assert.areEqual( 0, numericComparer.compare(null, null) );
			
			Assert.areEqual(-1, numericComparer.compare(null, number1) );
			Assert.areEqual( 1, numericComparer.compare(number1, null) );
		}
		
		[Test]
		public function compareHandleNaN() : void
		{
			var number1:Number = 123.456;
			
			Assert.areEqual( 0, numericComparer.compare(NaN, NaN) );
			
			Assert.areEqual(-1, numericComparer.compare(NaN, number1) );
			Assert.areEqual( 1, numericComparer.compare(number1, NaN) );
		}
		
		[Test]
		public function compareHandleNullAndNaN() : void
		{
			Assert.areEqual( 0, numericComparer.compare(NaN, NaN) );
			
			Assert.areEqual(-1, numericComparer.compare(null, NaN) );
			Assert.areEqual( 1, numericComparer.compare(NaN, null) );
		}
		
		[Test]
		public function compareHandleNonFinite() : void
		{
			Assert.areEqual( 0, numericComparer.compare( Infinity,  Infinity) );
			Assert.areEqual( 0, numericComparer.compare(-Infinity, -Infinity) );
			
			Assert.areEqual(-1, numericComparer.compare(-Infinity,  Infinity) );
			Assert.areEqual( 1, numericComparer.compare( Infinity, -Infinity) );
			
			Assert.areEqual(-1, numericComparer.compare( null,  Infinity) );
			Assert.areEqual(-1, numericComparer.compare( null, -Infinity) );
			Assert.areEqual( 1, numericComparer.compare( Infinity, null) );
			Assert.areEqual( 1, numericComparer.compare(-Infinity, null) );
			
			Assert.areEqual(-1, numericComparer.compare( NaN,  Infinity) );
			Assert.areEqual(-1, numericComparer.compare( NaN, -Infinity) );
			Assert.areEqual( 1, numericComparer.compare( Infinity, NaN) );
			Assert.areEqual( 1, numericComparer.compare(-Infinity, NaN) );
		}
		
		[Test]
		public function compareHandleAreSame() : void
		{
			var number1:Number = 123.456;
			
			Assert.areEqual( 0, numericComparer.compare(number1, number1) );
		}
		
		[Test]
		public function compareNumber() : void
		{
			var number1:Number = 123.456;
			var number2:Number = 123.456;
			var number3:Number = 654.321;
			
			Assert.areEqual( 0, numericComparer.compare(number1, number2) );
			
			Assert.areEqual(-1, numericComparer.compare(number2, number3) );
			Assert.areEqual( 1, numericComparer.compare(number3, number2) );
		}
		
		[Test]
		public function compareInt() : void
		{
			var number1:int = 123456;
			var number2:int = 123456;
			var number3:int = 654321;
			
			Assert.areEqual( 0, numericComparer.compare(number1, number2) );
			
			Assert.areEqual(-1, numericComparer.compare(number2, number3) );
			Assert.areEqual( 1, numericComparer.compare(number3, number2) );
		}
		
		[Test]
		[ExpectedError("ArgumentError")]
		public function compareFailureOnTypeMismatch() : void
		{
			var number1:Number = 123.456;
			var string:String = "123.456";
			
			numericComparer.compare(number1, string);
		}
		
		[Test]
		[ExpectedError("ArgumentError")]
		public function compareFailureOnTypeMismatch2() : void
		{
			var number1:Number = 123.456;
			var string:String = "123.456";
			
			numericComparer.compare(string, number1);
		}
		
		///////////////////////////////////////////
		// Equals Tests
		///////////////////////////////////////////
		
		[Test]
		public function equalsHandleNulls() : void
		{
			var number1:Number = 123.456;
			
			Assert.isTrue( numericComparer.equals(null, null) );
			
			Assert.isFalse( numericComparer.equals(null, number1) );
			Assert.isFalse( numericComparer.equals(number1, null) );
		}
		
		[Test]
		public function equalsHandleNaN() : void
		{
			var number1:Number = 123.456;
			
			Assert.isTrue( numericComparer.equals(NaN, NaN) );
			
			Assert.isFalse( numericComparer.equals(NaN, number1) );
			Assert.isFalse( numericComparer.equals(number1, NaN) );
		}
		
		[Test]
		public function equalsHandleNullAndNaN() : void
		{
			Assert.isFalse( numericComparer.equals(null, NaN) );
			Assert.isFalse( numericComparer.equals(NaN, null) );
		}
		
		[Test]
		public function equalsHandleNonFinite() : void
		{
			Assert.isTrue( numericComparer.equals( Infinity,  Infinity) );
			Assert.isTrue( numericComparer.equals(-Infinity, -Infinity) );
			
			Assert.isFalse( numericComparer.equals( Infinity, -Infinity) );
			Assert.isFalse( numericComparer.equals( null, Infinity) );
			Assert.isFalse( numericComparer.equals( NaN, Infinity) );
		}
		
		[Test]
		public function equalsHandleAreSame() : void
		{
			var number1:Number = 123.456;
			
			Assert.isTrue( numericComparer.equals(number1, number1) );
		}
		
		[Test]
		public function equalsNumber() : void
		{
			var number1:Number = 123.456;
			var number2:Number = 123.456;
			var number3:Number = 654.321;
			
			Assert.isTrue( numericComparer.equals(number1, number2) );
			
			Assert.isFalse( numericComparer.equals(number2, number3) );
			Assert.isFalse( numericComparer.equals(number3, number2) );
		}
		
		[Test]
		public function equalsInt() : void
		{
			var number1:int = 123456;
			var number2:int = 123456;
			var number3:int = 654321;
			
			Assert.isTrue( numericComparer.equals(number1, number2) );
			
			Assert.isFalse( numericComparer.equals(number2, number3) );
			Assert.isFalse( numericComparer.equals(number3, number2) );
		}
		
		[Test]
		[ExpectedError("ArgumentError")]
		public function equalsFailureOnTypeMismatch() : void
		{
			var number1:Number = 123.456;
			var string:String = "123.456";
			
			numericComparer.equals(number1, string);
		}
		
		[Test]
		[ExpectedError("ArgumentError")]
		public function equalsFailureOnTypeMismatch2() : void
		{
			var number1:Number = 123.456;
			var string:String = "123.456";
			
			numericComparer.equals(string, number1);
		}
		
	}
	
}