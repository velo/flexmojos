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
package net.flexmojos.oss.unitestingsupport.funit
{
	import funit.core.ITestListener;
	import funit.core.ResultState;
	import funit.core.TestCaseResult;
	import funit.core.TestName;
	import funit.core.TestOutput;
	import funit.core.TestResult;
	import funit.core.TestSuite;
	import funit.core.TestSuiteResult;

    import net.flexmojos.oss.unitestingsupport.ITestApplication;
	import net.flexmojos.oss.test.report.ErrorReport;
	import net.flexmojos.oss.test.report.TestCaseReport;
	import net.flexmojos.oss.unitestingsupport.SocketReporter;
	import net.flexmojos.oss.unitestingsupport.util.ClassnameUtil;
	import net.flexmojos.oss.unitestingsupport.UnitTestRunner;

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
            _socketReporter.sendResults();
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