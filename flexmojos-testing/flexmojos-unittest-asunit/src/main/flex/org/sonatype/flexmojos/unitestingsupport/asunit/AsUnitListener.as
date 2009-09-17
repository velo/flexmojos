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
package org.sonatype.flexmojos.unitestingsupport.asunit
{
	import asunit.errors.AssertionFailedError;
	import asunit.framework.Test;
	import asunit.framework.TestCase;
	import asunit.framework.TestResult;
	import asunit.framework.TestSuite;
	import asunit.framework.TestListener;
	
	import org.sonatype.flexmojos.test.report.ErrorReport;
	import org.sonatype.flexmojos.unitestingsupport.SocketReporter;
	import org.sonatype.flexmojos.unitestingsupport.util.ClassnameUtil;
	import org.sonatype.flexmojos.unitestingsupport.UnitTestRunner;

	public class AsUnitListener implements TestListener, UnitTestRunner
	{
		
		private var _socketReporter:SocketReporter;
		
		public function set socketReporter(socketReporter:SocketReporter):void {
			 this._socketReporter = socketReporter;
		}
		
		public function run(tests:Array):int {

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
		        result.addListener(new AsUnitListener(ClassnameUtil.getClassName(test), _socketReporter));

				var suite:TestSuite = new TestSuite();
				suite.addTest(testCase);
		        suite.setResult( result )
	    	    suite.run();
			}
	        
    	    
    	    return countTestCases;
		}
		
		private var className:String;
		
		public function AsUnitListener(className:String=null, socketReporter:SocketReporter=null) {
			this.className = className;
			this._socketReporter = socketReporter;
		}
		
    	/**
    	 * Called when a Test starts.
    	 * @param Test the test.
    	 */
    	public function startTest( test : Test ) : void
		{
			_socketReporter.addMethod( className, test.getCurrentMethod() );
		}
		
		/**
		 * Called when a Test ends.
		 * @param Test the test.
		 */
		public function endTest( test : Test ) : void
		{	
			_socketReporter.testFinished(className);
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

			_socketReporter.addError(className, test.getCurrentMethod(), failure);
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
			
			_socketReporter.addFailure(className, test.getCurrentMethod(), failure);
		}

	}
}