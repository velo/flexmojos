/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.unitestingsupport.asunit
{
	import asunit.errors.AssertionFailedError;
	import asunit.framework.Test;
	import asunit.framework.TestCase;
	import asunit.framework.TestResult;
	import asunit.framework.TestSuite;
	import asunit.framework.TestListener;
	
    import org.sonatype.flexmojos.unitestingsupport.ITestApplication;
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
		
        public function run( testApp:ITestApplication ):int
        {
            var tests:Array = testApp.tests;

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