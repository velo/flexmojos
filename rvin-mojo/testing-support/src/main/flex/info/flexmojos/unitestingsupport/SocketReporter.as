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
	
	import info.flexmojos.compile.test.report.ErrorReport;
	import info.flexmojos.compile.test.report.TestCaseReport;
	import info.flexmojos.compile.test.report.TestMethodReport;
	
	public class SocketReporter
	{

		private static const END_OF_TEST_RUN:String = "<endOfTestRun/>";
		private static const END_OF_TEST_ACK:String ="<endOfTestRunAck/>";
		
		[Inspectable]
		public static var port:uint = 1024;
		
		[Inspectable]
		public static var server:String = "127.0.0.1";
		
    	private static var socket:XMLSocket;

		private static var reports:Dictionary = new Dictionary();

		public static var totalTestCount : int;

    	private static var numTestsRun : int = 0;

		/**
		 * Called when an error occurs.
		 * @param test the Test that generated the error.
		 * @param error the Error.
		 */
		public static function addError( testName:String, methodName:String, error:ErrorReport ):void {
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
		public static function addMethod( testName:String, methodName:String ):void
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
		public static function addFailure( testName:String, methodName:String, failure:ErrorReport ):void
		{
			// Increment failure count.
			var report:TestCaseReport = getReport( testName );
			report.failures++;
			
			// Add the failure to the method.
			var methodObject:TestMethodReport = report.getMethod( methodName );
			methodObject.failure = failure;
		}
		
		public static function testFinished(testName:String):void
		{
			// If we have finished running all the tests send the results.
			if ( ++numTestsRun == totalTestCount )
			{
				sendResults();
   			}
		}
		

		/**
		 * Return the report Object from the internal report model for the
		 * currently executing Test.
		 * @param Test the test.
		 */
		public static function getReport( testName:String ):TestCaseReport
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
		private static function sendResults():void
		{
			// Open an XML socket.
			socket = new XMLSocket();
			socket.addEventListener( Event.CONNECT, handleConnect );
			socket.addEventListener( DataEvent.DATA, dataHandler );
   	   		socket.connect( server, port );
		}
		
		private static function handleConnect( event:Event ):void
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
		private static function dataHandler( event:DataEvent ):void
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
		private static function exit():void
		{
			// Close the socket.
			socket.close();
				
			// Execute the user's test complete function.
			fscommand( "quit" )
		}

		private static function formatQualifiedClassName( className:String ):String
		{
			var pattern:RegExp = /::/;
			
			return className.replace( pattern, "." );
		}

	}
}