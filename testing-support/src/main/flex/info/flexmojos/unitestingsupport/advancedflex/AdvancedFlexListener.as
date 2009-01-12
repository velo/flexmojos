/*
 * Flex-mojos is a set of maven plugins to allow maven users to compile, optimize, test and ... Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008 Marvin Herman Froeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.flexmojos.unitestingsupport.advancedflex
{
	import advancedflex.debugger.aut.framework.*;
	
	import info.flexmojos.unitestingsupport.util.ClassnameUtil;

	public class AdvancedFlexListener 
	{
		
		public static function run(tests:Array):int {

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
	    	    suite.startTest( new ProtectedConsole(ClassnameUtil.getClassName(test)) );
	        }
    	    
    	    return testsCount;
		}		

	}
}