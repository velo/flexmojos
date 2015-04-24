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
package {
	import asunit.framework.TestCase;

	public class ExampleTest extends TestCase {

		/**
	 	 * Test of whether or not class properly instantiated
	 	 */
	 	public function testInstantiated():void {
	 		assertTrue("Example instantiated", true);
	 	}

		/**
	 	 * Test that is born to lose.
	 	 */
	 	public function testFail():void {
	 		assertFalse("failing test", false);
	 	}

		/**
	 	 * Test the addition method on example
	 	 */
	 	public function testAddition():void {
	 		assertEquals( "Expected:5", 5, 2 + 3 );
	 	}
	}
}