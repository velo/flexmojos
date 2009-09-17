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
package org.sonatype.flexmojos.unitestingsupport.advancedflex
{
	import advancedflex.debugger.aut.framework.*;
	
	import org.sonatype.flexmojos.unitestingsupport.SocketReporter;
	import org.sonatype.flexmojos.unitestingsupport.util.ClassnameUtil;
	import org.sonatype.flexmojos.unitestingsupport.UnitTestRunner;

	public class AdvancedFlexListener implements UnitTestRunner
	{
		
		private var _socketReporter:SocketReporter;
		
		public function set socketReporter(socketReporter:SocketReporter):void {
			 this._socketReporter = socketReporter;
		}
		
		public function run(tests:Array):int {

			var suite:TestSuite = new TestSuite();
			
			var testsCount:int = 0;
			
			for each (var test:Class in tests)
			{
				var testCase:* = new test();
				if(testCase is TestCase)
				{
					suite.addTest( testCase );
					testsCount++;
				}
			}
	        
	        //suite will crash if launched without tests
	        //http://code.google.com/p/advancedflex/issues/detail?id=1
	        if(testsCount != 0) 
	        {
	    	    suite.startTest( new ProtectedConsole(ClassnameUtil.getClassName(test), _socketReporter) );
	        }
    	    
    	    return testsCount;
		}		

	}
}