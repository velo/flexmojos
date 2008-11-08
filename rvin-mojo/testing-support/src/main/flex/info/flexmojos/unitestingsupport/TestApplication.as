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
package info.flexmojos.unitestingsupport
{
	import flash.utils.getDefinitionByName;
	
	import funit.core.FUnitFramework;
	
	import info.flexmojos.unitestingsupport.flexunit.FlexUnitListener;
	
	import mx.core.Application;
	import mx.events.FlexEvent;
	
	public class TestApplication extends Application
	{
		
		private var tests:Array;
		
		public function set port(port:int):void {
			SocketReporter.port = port;
		}
		
		public function TestApplication()
		{
			tests = new Array();
			
			addEventListener(FlexEvent.CREATION_COMPLETE, runTests);
		}
		
		private function runTests(e:*):void 
		{
			SocketReporter.totalTestCount = tests.length;
			var testsScheduledToRun:int = 0;
			
			//flexunit supported
			if(getDefinitionByName("flexunit.framework.Test"))
			{
				testsScheduledToRun = FlexUnitListener.run(tests);
			}

			//funit supported			
			if(getDefinitionByName("funit.core.FUnitFramework"))
			{
				
			}
			
			//fluint supported
			if(getDefinitionByName("net.digitalprimates.fluint.tests.TestCase"))
			{
				
			}
		}

		/**
		 * Test to be run
		 * @param test the Test to run.
		 */
		public function addTest( test:Class ) : void
    	{
    	    tests.push(test);
		}
	}
}