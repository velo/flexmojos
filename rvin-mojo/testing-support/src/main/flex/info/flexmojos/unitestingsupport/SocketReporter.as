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
package info.flexmojos.unitestingsupport
{
	import flash.events.DataEvent;
	import flash.events.Event;
	import flash.net.XMLSocket;
	import flash.system.fscommand;
	import flash.utils.Dictionary;
	import flash.utils.getDefinitionByName;
	
	import info.flexmojos.compile.test.report.ErrorReport;
	import info.flexmojos.compile.test.report.TestCaseReport;
	import info.flexmojos.compile.test.report.TestMethodReport;
	import info.flexmojos.unitestingsupport.advancedflex.AdvancedFlexListener;
	import info.flexmojos.unitestingsupport.asunit.AsUnitListener;
	import info.flexmojos.unitestingsupport.flexunit.FlexUnitListener;
	import info.flexmojos.unitestingsupport.funit.FUnitListener;
	
	import mx.binding.utils.BindingUtils;
	
	public class SocketReporter
	{

		private const END_OF_TEST_RUN:String = "<endOfTestRun/>";
		private const END_OF_TEST_ACK:String ="<endOfTestRunAck/>";
		
		[Inspectable]
		public var port:uint = 1024;
		
		[Inspectable]
		public var server:String = "127.0.0.1";
		
    	private var socket:XMLSocket;

		private var reports:Dictionary = new Dictionary();

		[Bindable]
		public var totalTestCount : int = 0;

		[Bindable]
    	public var numTestsRun : int = 0;

		/**
		 * Called when an error occurs.
		 * @param test the Test that generated the error.
		 * @param error the Error.
		 */
		public function addError( testName:String, methodName:String, error:ErrorReport ):void {
			// Increment error count.
			var report:TestCaseReport = getReport( testName );
			report.errors++;
			
			// Add the error to the method.
			var methodObject:TestMethodReport = report.getMethod( methodName );
			methodObject.error = error;
		}

		/**
		 * Add the currently executing method on a Test to the internal report
		 * model.
		 * @param test the Test.
		 */
		public function addMethod( testName:String, methodName:String ):void
		{
			var reportObject:TestCaseReport = getReport( testName );
			reportObject.getMethod(methodName);
			reportObject.tests++;
		}
		
		/**
		 * Called when a failure occurs.
		 * @param test the Test that generated the failure.
		 * @param error the failure.
		 */
		public function addFailure( testName:String, methodName:String, failure:ErrorReport ):void
		{
			// Increment failure count.
			var report:TestCaseReport = getReport( testName );
			report.failures++;
			
			// Add the failure to the method.
			var methodObject:TestMethodReport = report.getMethod( methodName );
			methodObject.failure = failure;
		}
		
		public function testFinished(testName:String, timeTaken:int = 0):void
		{
			var reportObject:TestCaseReport = reports[ testName ];
			reportObject.time = timeTaken;

			// If we have finished running all the tests send the results.
			++numTestsRun;
		}
		

		/**
		 * Return the report Object from the internal report model for the
		 * currently executing Test.
		 * @param Test the test.
		 */
		public function getReport( testName:String ):TestCaseReport
		{
			var reportObject:TestCaseReport;
			
			// Check we have a report Object for the executing Test, if not
			// create a new one.
			if ( reports[ testName ] )
			{
				reportObject = reports[ testName ];
			}
			else
			{
				reportObject = new TestCaseReport();
				reportObject.name = testName;
				
				reports[ testName ] = reportObject;
			}
			
			return reportObject;
		}

		/**
		 * Sends the results. This sends the reports back to the controlling Ant
		 * task using an XMLSocket.
		 */
		private function sendResults():void
		{
			// Open an XML socket.
			socket = new XMLSocket();
			socket.addEventListener( Event.CONNECT, handleConnect );
			socket.addEventListener( DataEvent.DATA, dataHandler );
   	   		socket.connect( server, port );
		}
		
		private function handleConnect( event:Event ):void
		{
			for ( var className:String in reports )
			{
				var testReport:TestCaseReport = reports[ className ];
				// Create the XML report.
				var xml:XML = testReport.toXml();
				
				var xmlString:String = xml.toXMLString();
				
				// Send the XML report.
				socket.send( xmlString );
			}
			
			// Send the end of reports terminator.
			socket.send( END_OF_TEST_RUN );
		}
		
		/**
		 * Event listener to handle data received on the socket.
		 * @param event the DataEvent.
		 */
		private function dataHandler( event:DataEvent ):void
		{
			var data:String = event.data;

			// If we received an acknowledgement finish-up.			
			if ( data == END_OF_TEST_ACK )
			{
				exit();
   			}
		}

		/**
		 * Exit the test runner and close the player.
		 */
		private function exit():void
		{
			// Close the socket.
			socket.close();
				
			// Execute the user's test complete function.
			fscommand( "quit" )
		}

		private function formatQualifiedClassName( className:String ):String
		{
			var pattern:RegExp = /::/;
			
			return className.replace( pattern, "." );
		}

		public function runTests(tests:Array):void 
		{
			//flexunit supported
			if(getDefinitionByName("flexunit.framework.Test"))
			{
				totalTestCount += FlexUnitListener.run(tests);
			}

			//funit supported			
			if(getDefinitionByName("funit.core.FUnitFramework"))
			{
				totalTestCount += FUnitListener.run(tests);
			}
			
			//fluint supported
			if(getDefinitionByName("net.digitalprimates.fluint.tests.TestCase"))
			{
				//too much complicated, didn't figure out how to run a test w/o UI
				//testsScheduledToRun += FluintListener.run(tests);
			}

			//asunit supported
			if(getDefinitionByName("asunit.framework.Test"))
			{
				totalTestCount += AsUnitListener.run(tests);
			}

			//advancedflex supported
			if(getDefinitionByName("advancedflex.debugger.aut.framework.Test"))
			{
				totalTestCount += AdvancedFlexListener.run(tests);
			}
		}
		
		private static var instance:SocketReporter;
		
		public static function getInstance():SocketReporter {
			if(instance == null) {
				instance = new SocketReporter();
				
				var checkIsDone:Function = function (e:*):void {
					if(instance.totalTestCount == 0) {
						return;
					}
					if(instance.totalTestCount == instance.numTestsRun) {
						instance.sendResults();
					}
				};
				
				BindingUtils.bindSetter(checkIsDone, instance, "numTestsRun");
				BindingUtils.bindSetter(checkIsDone, instance, "totalTestCount");
			}
			return instance;
		}

	}
}