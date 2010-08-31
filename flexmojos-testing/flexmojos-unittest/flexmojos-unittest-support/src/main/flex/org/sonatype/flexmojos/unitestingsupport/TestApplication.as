/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.unitestingsupport
{
    import flash.system.fscommand;

    import mx.core.Application;
    import mx.events.FlexEvent;

    public class TestApplication extends Application implements ITestApplication
    {

        private var _componentUnderTest:*;

        private var _tests:Array;

        private static var socketReporter:SocketReporter = SocketReporter.getInstance();

        private static var controlSocket:ControlSocket = ControlSocket.getInstance();

        public function set port( port:int ):void
        {
            socketReporter.port = port;
        }

        public function set controlPort( port:int ):void
        {
            controlSocket.port = port;
        }

        public function TestApplication()
        {
            this._tests = new Array();

            addEventListener( FlexEvent.CREATION_COMPLETE, runTests );
        }

        private function runTests( e:* ):void
        {
            controlSocket.connect( this );
            socketReporter.runTests( this );
        }

        /**
         * Test to be run
         * @param test the Test to run.
         */
        public function addTest( test:Class ):void
        {
            _tests.push( test );
            trace( "Testing " + test );
        }

        public function get tests():Array
        {
            return this._tests;
        }

        public function get componentUnderTest():*
        {
            return _componentUnderTest;
        }

        public function set componentUnderTest( cmp:* ):void
        {
            if ( this._componentUnderTest != null )
            {
                removeChild( this._componentUnderTest );
                this._componentUnderTest = null;
            }

            if ( cmp != null )
            {
                this._componentUnderTest = cmp;
                addChild( this._componentUnderTest );
            }
        }


        public function killApplication():void
        {
            fscommand( "quit" );
        }

    }
}