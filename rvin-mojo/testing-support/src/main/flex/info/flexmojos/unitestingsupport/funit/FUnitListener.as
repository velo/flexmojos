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
package info.flexmojos.unitestingsupport.funit
{
	import funit.core.ITestListener;
	import funit.core.ResultState;
	import funit.core.TestCaseResult;
	import funit.core.TestName;
	import funit.core.TestOutput;
	import funit.core.TestResult;
	import funit.core.TestSuite;
	import funit.core.TestSuiteResult;
	
	import info.flexmojos.compile.test.report.ErrorReport;
	import info.flexmojos.compile.test.report.TestCaseReport;
	import info.flexmojos.unitestingsupport.SocketReporter;
	import info.flexmojos.unitestingsupport.util.ClassnameUtil;
	
	public class FUnitListener implements ITestListener
	{
		
		private static var socketReporter:SocketReporter = SocketReporter.getInstance();
		
		public static function run(tests:Array):int {
			
			var testCount:int = 0;
			
			for each (var test:Class in tests)
			{
				var suite:TestSuite = new TestSuite();
				suite.addDefinition(test);
				if(suite.testCount != 0) 
				{
					testCount += suite.testCount;
					
					var classname:String = ClassnameUtil.getClassName(test);
					var testCaseReport:TestCaseReport = socketReporter.getReport(classname);
					
					var listener:ITestListener = new FUnitListener(testCaseReport);
					suite.run(listener);
				}
			}
			
	        
    	    return testCount;
		}
		
		private var testCaseReport:TestCaseReport;
		
		private var methodName:String;

		public function FUnitListener(testCaseReport:TestCaseReport) 
		{
			this.testCaseReport = testCaseReport;
		}

		/**
		 * @param name
		 * @param testCount
		 */
		public function runStarted( name:String, testCount:int ) : void 
		{
			
		}
		
		/**
		 * @param result
		 */
		public function runFinished( result:TestResult ) : void 
		{
			
		}
		
		/**
		 * @param error
		 */
		public function runFinishedError( error:Error ) : void 
		{
			
		}
		
		/**
		 * @param testName
		 */
		public function testStarted( testName:TestName ) : void 
		{
			methodName = testName.name;
			socketReporter.addMethod( testCaseReport.name, methodName );
		}
		
		/**
		 * @param result
		 */
		public function testFinished( result:TestCaseResult ) : void 
		{
			if(result.isSuccess)
			{
				socketReporter.testFinished(testCaseReport.name);
			}
			else
			{
				if(result.resultState == ResultState.Failure) {
					var failure:ErrorReport = new ErrorReport();
					failure.type = "Not available at FUnit";
					failure.message = result.message;
					failure.stackTrace = result.stackTrace;
					
					socketReporter.addFailure(testCaseReport.name, methodName, failure);
				}
					
				if(result.resultState == ResultState.Error) {
					var error:ErrorReport = new ErrorReport();
					error.type = "Not available at FUnit";
					error.message = result.message;
					error.stackTrace = result.stackTrace;
					
					socketReporter.addError(testCaseReport.name, methodName, error);
				}
					
			}
		}
		
		/**
		 * @param testName
		 */
		public function suiteStarted( testName:TestName ) : void 
		{
			
		}
		
		/**
		 * @param result
		 */
		public function suiteFinished( result:TestSuiteResult ) : void 
		{
			
		}
		
		/**
		 * @param error
		 */
		public function unhandledError( error:Error ) : void 
		{
			var failure:ErrorReport = new ErrorReport();
			failure.type = error.name;
			failure.message = error.message;
			failure.stackTrace = error.getStackTrace();

			socketReporter.addError(testCaseReport.name, methodName, failure);
		}
		
		/**
		 * @param testOutput
		 */
		public function testOutput( testOutput:TestOutput ) : void 
		{
			
		}	

	}
}