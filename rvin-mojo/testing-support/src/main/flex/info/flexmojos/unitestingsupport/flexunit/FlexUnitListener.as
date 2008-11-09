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
package info.flexmojos.unitestingsupport.flexunit
{
	import flexunit.framework.*;
	
	import info.flexmojos.compile.test.report.ErrorReport;
	import info.flexmojos.unitestingsupport.SocketReporter;
	import info.flexmojos.unitestingsupport.util.ClassnameUtil;

	public class FlexUnitListener implements TestListener
	{
		
		
		public static function run(tests:Array):int {
    		var result:TestResult = new TestResult();
	        result.addListener(new FlexUnitListener());

			var suite:TestSuite = new TestSuite();
			
			for each (var test:Class in tests)
			{
				if(test is Test)
				{
					suite.addTestSuite(test);
				}
			}
	        
    	    suite.runWithResult( result );
    	    
    	    return suite.countTestCases();
		}
		
    	/**
    	 * Called when a Test starts.
    	 * @param Test the test.
    	 */
    	public function startTest( test : Test ) : void
		{
			SocketReporter.addMethod( test.className, test[ "methodName" ] );
		}
		
		/**
		 * Called when a Test ends.
		 * @param Test the test.
		 */
		public function endTest( test : Test ) : void
		{	
			SocketReporter.testFinished(test.className);
		}
	
		/**
		 * Called when an error occurs.
		 * @param test the Test that generated the error.
		 * @param error the Error.
		 */
		public function addError( test : Test, error : Error ) : void
		{
			var failure:ErrorReport = new ErrorReport();
			failure.type = ClassnameUtil.getClassName(error);
			failure.message = error.message;
			failure.stackTrace = error.getStackTrace();

			SocketReporter.addError(test.className, test[ "methodName" ], failure);
		}

		/**
		 * Called when a failure occurs.
		 * @param test the Test that generated the failure.
		 * @param error the failure.
		 */
		public function addFailure( test : Test, error : AssertionFailedError ) : void
		{
			var failure:ErrorReport = new ErrorReport();
			failure.type = ClassnameUtil.getClassName(error);
			failure.message = error.message;
			failure.stackTrace = error.getStackTrace();
			
			SocketReporter.addFailure(test.className, test[ "methodName" ], failure);
		}

	}
}