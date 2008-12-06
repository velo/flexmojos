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
package info.flexmojos.unitestingsupport.asunit
{
	import asunit.errors.AssertionFailedError;
	import asunit.framework.*;
	
	import info.flexmojos.compile.test.report.ErrorReport;
	import info.flexmojos.unitestingsupport.SocketReporter;
	import info.flexmojos.unitestingsupport.util.ClassnameUtil;

	public class AsUnitListener implements TestListener
	{
		
		private static var socketReporter:SocketReporter = SocketReporter.getInstance();
		
		public static function run(tests:Array):int {

			var countTestCases:int;
			
			for each (var test:Class in tests)
			{
				var testCase:* = new test();
				if( !(testCase is TestCase))
				{
					continue;
				}
				countTestCases++;

	    		var result:TestResult = new TestResult();
		        result.addListener(new AsUnitListener(ClassnameUtil.getClassName(test)));

				var suite:TestSuite = new TestSuite();
				suite.addTest(testCase);
		        suite.setResult( result )
	    	    suite.run();
			}
	        
    	    
    	    return countTestCases;
		}
		
		private var className:String;
		
		public function AsUnitListener(className:String) {
			this.className = className;
		}
		
    	/**
    	 * Called when a Test starts.
    	 * @param Test the test.
    	 */
    	public function startTest( test : Test ) : void
		{
			socketReporter.addMethod( className, test.getCurrentMethod() );
		}
		
		/**
		 * Called when a Test ends.
		 * @param Test the test.
		 */
		public function endTest( test : Test ) : void
		{	
			socketReporter.testFinished(className);
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

			socketReporter.addError(className, test.getCurrentMethod(), failure);
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
			
			socketReporter.addFailure(className, test.getCurrentMethod(), failure);
		}

	}
}