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
    import flash.events.ProgressEvent;
    import flash.net.Socket;
    import flash.system.fscommand;

    import org.sonatype.flexmojos.test.monitor.CommConstraints;

    public class ControlSocket
    {

        [Inspectable]
        public var port:uint = 1024;

        [Inspectable]
        public var server:String = "127.0.0.1";

        private var socket:Socket;

        private var closeController:CloseController = CloseController.getInstance();

        private var exitFunction:Function;

        public function connect( testApplication:ITestApplication ):void
        {
            exitFunction = testApplication.killApplication;


            socket = new Socket();
            socket.addEventListener( ProgressEvent.SOCKET_DATA, dataHandler );
            socket.addEventListener( Event.CLOSE, exitFP );
            socket.connect( server, port );
        }

        private function exitFP( event:* ):void
        {
            //Exiting
            trace( "Exiting" );
            exitFunction();
        }

        /**
         * Event listener to handle data received on the socket.
         * @param event the DataEvent.
         */
        private function dataHandler( event:* ):void
        {

            var data:String = socket.readUTFBytes( socket.bytesAvailable );
            trace( "Data handler received data: " + data );

            var status:String = CommConstraints.STATUS + CommConstraints.EOL;

            if ( data == status )
            {
                if ( closeController.canClose )
                {
                    trace( "Replying FINISHED" );
                    socket.writeUTFBytes( CommConstraints.FINISHED + CommConstraints.EOL );
                    socket.flush();
                }
                else
                {
                    trace( "Replying OK" );
                    socket.writeUTFBytes( CommConstraints.OK + CommConstraints.EOL );
                    socket.flush();
                }

            }
        }

        private static var instance:ControlSocket;

        public static function getInstance():ControlSocket
        {
            if ( instance == null )
            {
                instance = new ControlSocket();
            }
            return instance;
        }

    }
}