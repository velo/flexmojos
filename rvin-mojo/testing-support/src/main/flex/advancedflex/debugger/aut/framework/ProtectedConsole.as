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

//All methods I need to override are internal, so need to use the same package
package advancedflex.debugger.aut.framework
{
	
	import advancedflex.io.TraceDataOutput;
	
	import flash.utils.IDataOutput;
	
	import info.flexmojos.compile.test.report.ErrorReport;
	import info.flexmojos.compile.test.report.TestCaseReport;
	import info.flexmojos.unitestingsupport.SocketReporter;
	
	public class ProtectedConsole extends Console
	{

		private var test:TestCaseReport;
		
		private var _out:TraceDataOutput = new TraceDataOutput();
		
		private var error:ErrorReport;

		private static var socketReporter:SocketReporter = SocketReporter.getInstance();
		
		public function ProtectedConsole(className:String)
		{
			test = socketReporter.getReport(className);
		}

		protected override function get out():IDataOutput {
			return _out;
		}
		
		override internal function printMethodFooter(method:String, state:String):void
		{
			super.printMethodFooter(method, state);
			if("Error" == state) {
				socketReporter.addError(test.name, method, error);
			} else if("Failure" == state) {
				socketReporter.addFailure(test.name, method, error);
			} else {
				socketReporter.addMethod(test.name, method);
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
			socketReporter.testFinished(test.name);
		}

	}
}