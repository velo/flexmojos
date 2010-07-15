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