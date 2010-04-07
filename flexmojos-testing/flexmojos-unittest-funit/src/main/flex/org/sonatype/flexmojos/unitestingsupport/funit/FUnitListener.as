/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.unitestingsupport.funit
{
	import funit.core.ITestListener;
	import funit.core.ResultState;
	import funit.core.TestCaseResult;
	import funit.core.TestName;
	import funit.core.TestOutput;
	import funit.core.TestResult;
	import funit.core.TestSuite;
	import funit.core.TestSuiteResult;

    import org.sonatype.flexmojos.unitestingsupport.ITestApplication;
	import org.sonatype.flexmojos.test.report.ErrorReport;
	import org.sonatype.flexmojos.test.report.TestCaseReport;
	import org.sonatype.flexmojos.unitestingsupport.SocketReporter;
	import org.sonatype.flexmojos.unitestingsupport.util.ClassnameUtil;
	import org.sonatype.flexmojos.unitestingsupport.UnitTestRunner;

	public class FUnitListener implements ITestListener, UnitTestRunner
	{

		private var _socketReporter:SocketReporter;
		
		public function set socketReporter(socketReporter:SocketReporter):void {
			 this._socketReporter = socketReporter;
		}

        public function run( testApp:ITestApplication ):int
        {
            var tests:Array = testApp.tests;

			var testCount:int=0;

			for each (var test:Class in tests)
			{
				var suite:TestSuite=new TestSuite();
				suite.addDefinition(test);
				if (suite.testCount != 0)
				{
					testCount+=suite.testCount;

					var classname:String=ClassnameUtil.getClassName(test);
					var testCaseReport:TestCaseReport=_socketReporter.getReport(classname);

					var listener:ITestListener=new FUnitListener(testCaseReport,_socketReporter);
					suite.run(listener);
				}
			}


			return testCount;
		}

		private var testCaseReport:TestCaseReport;

		private var methodName:String;

		public function FUnitListener(testCaseReport:TestCaseReport=null, socketReporter:SocketReporter=null)
		{
			this.testCaseReport=testCaseReport;
			this._socketReporter = socketReporter;
		}

		/**
		 * @param name
		 * @param testCount
		 */
		public function runStarted(name:String, testCount:int):void
		{

		}

		/**
		 * @param result
		 */
		public function runFinished(result:TestResult):void
		{

		}

		/**
		 * @param error
		 */
		public function runFinishedError(error:Error):void
		{

		}

		/**
		 * @param testName
		 */
		public function testStarted(testName:TestName):void
		{
			methodName=testName.name;
			_socketReporter.addMethod(testCaseReport.name, methodName);
		}

		/**
		 * @param result
		 */
		public function testFinished(result:TestCaseResult):void
		{
			if (result.isSuccess)
			{
				_socketReporter.testFinished(testCaseReport.name);
			}
			else
			{
				if (result.resultState == ResultState.Failure)
				{
					var failure:ErrorReport=new ErrorReport();
					failure.type="Not available at FUnit";
					failure.message=result.message;
					failure.stackTrace=result.stackTrace;

					_socketReporter.addFailure(testCaseReport.name, methodName, failure);
				}

				if (result.resultState == ResultState.Error)
				{
					var error:ErrorReport=new ErrorReport();
					error.type="Not available at FUnit";
					error.message=result.message;
					error.stackTrace=result.stackTrace;

					_socketReporter.addError(testCaseReport.name, methodName, error);
				}

			}
		}

		/**
		 * @param testName
		 */
		public function suiteStarted(testName:TestName):void
		{

		}

		/**
		 * @param result
		 */
		public function suiteFinished(result:TestSuiteResult):void
		{

		}

		/**
		 * @param error
		 */
		public function unhandledError(error:Error):void
		{
			var failure:ErrorReport=new ErrorReport();
			failure.type=error.name;
			failure.message=error.message;
			failure.stackTrace=error.getStackTrace();

			_socketReporter.addError(testCaseReport.name, methodName, failure);
		}

		/**
		 * @param testOutput
		 */
		public function testOutput(testOutput:TestOutput):void
		{

		}

	}
}