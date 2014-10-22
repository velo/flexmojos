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
/**
 * @author Seven
 */
package  {

	import flexunit.framework.Assert;
	import flash.errors.IOError;
	import main;
	
	public class AnnotatedTest {
		
		[Test]
		public function addition():void { 
		   Assert.assertEquals(12, 7 + 5); 
		}
		
		[Test(expects="flash.errors.IOError")] 
		public function doIOError():void { 
		   //a test which causes an IOError }Or
		   throw new IOError(); 
		}
		
		[Test] 
		public function hiTest():void { 
		   Assert.assertEquals("hi", main.hi() ); 
		}
	}

}