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
package FlexMaven
{

	import flexunit.framework.TestCase;

	public class TestApp extends TestCase
	{

		/**
		 * Tests our greeting() method
		 */
		public function testGreeting():void
		{
			var name:String="Buck Rogers";
			var expectedGreeting:String="Hello, Buck Rogers";

			var result:String=App.greeting(name);
			assertEquals("Greeting is incorrect", expectedGreeting, result);
		}

		/**
		 * Tests our greeting() method
		 */
		public function testGreeting2():void
		{
			var name:String="IAmAnIdiot";
			var expectedGreeting:String="NoYouAreNotbob";

			var result:String=App.greeting(name);
			assertEquals("Greeting is incorrect", expectedGreeting, result);
		}

		/**
		 * Tests our greeting() method
		 */
		public function testGreeting3():void
		{
			var name:String="alkdjfakldsfj";
			var expectedGreeting:String="adjfaklsdfjasklfj";

			var result:String=App.greeting(name);
			assertEquals("Greeting is incorrect", expectedGreeting, result);
		}

		/**
		 * Tests our greeting() method
		 */
		public function testGreeting4():void
		{
			var name:String="akldjfakldsfjasdklfjc,vnz";
			var expectedGreeting:String="akdfjla";

			var result:String=App.greeting(name);
			assertEquals("Greeting is incorrect", expectedGreeting, result);
		}
	}
}
