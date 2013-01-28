/**
 * Flexmojos is a set of maven goals to allow maven users to compile,
 * optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
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
package net.flexmojos.oss.unitestingsupport.flexunit
{
	import flexunit.framework.*; 
	
    import net.flexmojos.oss.unitestingsupport.ITestApplication;
	import net.flexmojos.oss.unitestingsupport.UnitTestRunner;
	import net.flexmojos.oss.test.report.ErrorReport;
	import net.flexmojos.oss.unitestingsupport.SocketReporter;
	import net.flexmojos.oss.unitestingsupport.util.ClassnameUtil;

	public class FlexUnitListener implements TestListener, UnitTestRunner
	{

        private var totalTests:int;
        private var testsExecuted:int;

		private var _socketReporter:SocketReporter;

        public function FlexUnitListener() {
        }

        public function set socketReporter(socketReporter:SocketReporter):void {
			 this._socketReporter = socketReporter;
		}
		
        public function run( testApp:ITestApplication ):int
        {

            var tests:Array = testApp.tests;

    		var result:TestResult = new TestResult();
	        result.addListener(this);

			var suite:TestSuite = new TestSuite();
			
			for each (var test:Class in tests)
			{
				var testCase:* = new test();
				if(testCase is Test)
				{
					suite.addTestSuite(test);
				}
			}

            testsExecuted = 0;

    	    suite.runWithResult( result );

            totalTests = suite.countTestCases();

            checkFinished();

            return totalTests;
		}
		
    	/**
    	 * Called when a Test starts.
    	 * @param test the test.
    	 */
    	public function startTest( test : Test ) : void
		{
			_socketReporter.addMethod( test.className, test[ "methodName" ] );
		}
		
		/**
		 * Called when a Test ends.
		 * @param test the test.
		 */
		public function endTest( test : Test ) : void
		{	
			_socketReporter.testFinished(test.className);

            testsExecuted++;

            checkFinished();
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

			_socketReporter.addError(test.className, test[ "methodName" ], failure);
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
			
			_socketReporter.addFailure(test.className, test[ "methodName" ], failure);
		}

        protected function checkFinished():void {
            if(totalTests == testsExecuted) {
                _socketReporter.sendResults();
            }
        }

	}
}