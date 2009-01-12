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
package com.adobe.example
{
	import flexunit.framework.TestCase;
	import com.adobe.example.Calculator;
	
	public class TestCalculator extends TestCase
	{
		private var calculator : Calculator;
		
		/**
		 * Constructor.
		 * @param methodName the name of the individual test to run.
		 */
		public function TestCalculator( methodName : String = null )
		{
			super( methodName );
		}
	
		/**
		 * @see flexunit.framework.TestCase#setUp().
		 */
		override public function setUp() : void
		{
			calculator = new Calculator();
		}
		
		/**
		 * @see flexunit.framework.TestCase#tearDown().
		 */
		override public function tearDown() : void
		{
			calculator = null;
		}

		public function testMultiplyPass() : void
		{
			var result : Number = calculator.multiply( 2, 4 );
			assertEquals( 8, result );
		}

		public function testMultiplyFail() : void
		{
			var result : Number = calculator.multiply( 2, 4 );
			assertEquals( 10, result );
		}

		public function testError() : void
		{
			throw new Error("An error");
		}

	}
}