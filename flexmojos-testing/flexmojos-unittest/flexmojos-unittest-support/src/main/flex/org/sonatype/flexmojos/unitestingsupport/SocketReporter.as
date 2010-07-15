/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.unitestingsupport
{
    import flash.events.DataEvent;
    import flash.events.Event;
    import flash.net.XMLSocket;
    import flash.utils.Dictionary;
    import flash.utils.getDefinitionByName;

    import mx.binding.utils.BindingUtils;

    import org.sonatype.flexmojos.test.monitor.CommConstraints;
    import org.sonatype.flexmojos.test.report.ErrorReport;
    import org.sonatype.flexmojos.test.report.TestCaseReport;
    import org.sonatype.flexmojos.test.report.TestMethodReport;

    public class SocketReporter
    {

        [Inspectable]
        public var port:uint = 1024;

        [Inspectable]
        public var server:String = "127.0.0.1";

        private var socket:XMLSocket;

        private var reports:Dictionary = new Dictionary();

        [Bindable]
        public var totalTestCount:int = 0;

        [Bindable]
        public var numTestsRun:int = 0;

        private var closeController:CloseController = CloseController.getInstance();

        /**
         * Called when an error occurs.
         * @param test the Test that generated the error.
         * @param error the Error.
         */
        public function addError( testName:String, methodName:String, error:ErrorReport ):void
        {
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
            reportObject.getMethod( methodName );
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

        public function testFinished( testName:String, timeTaken:int = 0 ):void
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
            socket.send( CommConstraints.END_OF_TEST_RUN );
        }

        /**
         * Event listener to handle data received on the socket.
         * @param event the DataEvent.
         */
        private function dataHandler( event:DataEvent ):void
        {
            var data:String = event.data;

            // If we received an acknowledgement finish-up.			
            if ( data == CommConstraints.ACK_OF_TEST_RESULT )
            {
                exit();
            }
        }

        /**
         * Exit the test runner and enabling to close the player.
         */
        private function exit():void
        {
            // Close the socket.
            if ( socket )
            {
                socket.close();
            }

            // Enabling to close flashplayer
            closeController.canClose = true;
        }

        private function formatQualifiedClassName( className:String ):String
        {
            var pattern:RegExp = /::/;

            return className.replace( pattern, "." );
        }

        public function runTests( testApplication:ITestApplication ):void
        {
            var def:* = null;

            //flexunit supported
            if ( ( def = tryGetDefinitionByName( "org.sonatype.flexmojos.unitestingsupport.flexunit.FlexUnitListener" ) ) != null )
            {
                trace( "Running tests using Flexunit" );
            }

            //flexunit4 supported
            else if ( ( def = tryGetDefinitionByName( "org.sonatype.flexmojos.unitestingsupport.flexunit4.FlexUnit4Listener" ) ) != null )
            {
                trace( "Running tests using Flexunit4" );
            }

            //funit supported			
            else if ( ( def = tryGetDefinitionByName( "org.sonatype.flexmojos.unitestingsupport.funit.FUnitListener" ) ) != null )
            {
                trace( "Running tests using FUnit" );
            }

            //fluint supported
            else if ( ( def = tryGetDefinitionByName( "org.sonatype.flexmojos.unitestingsupport.fluint.FluintListener" ) ) != null )
            {
                trace( "Running tests using Fluint" );
            }

            //asunit supported
            else if ( ( def = tryGetDefinitionByName( "org.sonatype.flexmojos.unitestingsupport.asunit.AsUnitListener" ) ) != null )
            {
                trace( "Running tests using asunit" );
            }

            //advancedflex supported
            else if ( ( def = tryGetDefinitionByName( "org.sonatype.flexmojos.unitestingsupport.advancedflex.AdvancedFlexListener" ) ) != null )
            {
                trace( "Running tests using Advanced Flex tests" );
            }

            //mustella
            else if ( ( def = tryGetDefinitionByName( "org.sonatype.flexmojos.unitestingsupport.mustella.MustellaListener" ) ) != null )
            {
                trace( "Running tests using Mustella" );
            }

            //not found
            else
            {
                trace( "No test runner found, exiting" );
                exit();
            }

            var runner:UnitTestRunner = new def();
            runner.socketReporter = this;
            totalTestCount = runner.run( testApplication );
            trace( "Running " + totalTestCount + " tests" );

            if ( totalTestCount == 0 )
            {
                trace( "No tests to run, exiting" );
                exit();
            }
        }

        private function tryGetDefinitionByName( classname:String ):Class
        {
            try
            {
                return getDefinitionByName( classname ) as Class;
            }
            catch ( e:ReferenceError )
            {
            }
            return null;
        }

        private static var instance:SocketReporter;

        public static function getInstance():SocketReporter
        {
            if ( instance == null )
            {
                instance = new SocketReporter();

                var checkIsDone:Function = function( e:* ):void
                    {
                        if ( instance.totalTestCount == 0 )
                        {
                            return;
                        }
                        if ( instance.totalTestCount == instance.numTestsRun )
                        {
                            instance.sendResults();
                        }
                    };

                BindingUtils.bindSetter( checkIsDone, instance, "numTestsRun" );
                BindingUtils.bindSetter( checkIsDone, instance, "totalTestCount" );
            }
            return instance;
        }

    }
}