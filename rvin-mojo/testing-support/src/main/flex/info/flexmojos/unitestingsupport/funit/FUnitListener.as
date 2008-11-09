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
					var testCaseReport:TestCaseReport = SocketReporter.getReport(classname);
					
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
			SocketReporter.addMethod( testCaseReport.name, methodName );
		}
		
		/**
		 * @param result
		 */
		public function testFinished( result:TestCaseResult ) : void 
		{
			if(result.isSuccess)
			{
				SocketReporter.testFinished(testCaseReport.name);
			}
			else
			{
				if(result.resultState == ResultState.Failure) {
					var failure:ErrorReport = new ErrorReport();
					failure.type = "Not available at FUnit";
					failure.message = result.message;
					failure.stackTrace = result.stackTrace;
					
					SocketReporter.addFailure(testCaseReport.name, methodName, failure);
				}
					
				if(result.resultState == ResultState.Error) {
					var error:ErrorReport = new ErrorReport();
					error.type = "Not available at FUnit";
					error.message = result.message;
					error.stackTrace = result.stackTrace;
					
					SocketReporter.addError(testCaseReport.name, methodName, error);
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

			SocketReporter.addError(testCaseReport.name, methodName, failure);
		}
		
		/**
		 * @param testOutput
		 */
		public function testOutput( testOutput:TestOutput ) : void 
		{
			
		}	

	}
}