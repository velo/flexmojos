/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
//All methods I need to override are internal, so need to use the same package
package advancedflex.debugger.aut.framework
{
	
	import advancedflex.io.TraceDataOutput;
	
	import flash.utils.IDataOutput;
	
	import org.sonatype.flexmojos.test.report.ErrorReport;
	import org.sonatype.flexmojos.test.report.TestCaseReport;
	import org.sonatype.flexmojos.unitestingsupport.SocketReporter;
	
	public class ProtectedConsole extends Console
	{

		private var test:TestCaseReport;
		
		private var _out:TraceDataOutput = new TraceDataOutput();
		
		private var error:ErrorReport;

		private var _socketReporter:SocketReporter;
		
		public function ProtectedConsole(className:String, socketReporter:SocketReporter)
		{
			test = socketReporter.getReport(className);
			this._socketReporter = socketReporter;
		}

		protected override function get out():IDataOutput {
			return _out;
		}
		
		override internal function printMethodFooter(method:String, state:String):void
		{
			super.printMethodFooter(method, state);
			if("Error" == state) {
				_socketReporter.addError(test.name, method, error);
			} else if("Failure" == state) {
				_socketReporter.addFailure(test.name, method, error);
			} else {
				_socketReporter.addMethod(test.name, method);
			}
		}
		
		override internal function printStackTrace(error:Error):void
		{
			super.printStackTrace(error);
			this.error = new ErrorReport(error);
		}
		
		override internal function printTestCaseFooter(testCase:TestCase, success:int, failure:int, error:int):void
		{
			super.printTestCaseFooter(testCase, success, failure, error);
			_socketReporter.testFinished(test.name);
		}

	}
}