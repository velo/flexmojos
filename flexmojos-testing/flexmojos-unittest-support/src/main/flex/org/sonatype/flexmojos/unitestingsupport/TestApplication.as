/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.unitestingsupport
{
	import mx.core.Application;
	import mx.events.FlexEvent;
	
	public class TestApplication extends Application
	{
		
		private var tests:Array;
		
		private static var socketReporter:SocketReporter = SocketReporter.getInstance();

		private static var controlSocket:ControlSocket = ControlSocket.getInstance();
		
		public function set port(port:int):void {
			socketReporter.port = port;
		}

		public function set controlPort(port:int):void {
			controlSocket.port = port;
		}
		
		public function TestApplication()
		{
			this.tests = new Array();
			
			addEventListener(FlexEvent.CREATION_COMPLETE, runTests);
		}
		
		private function runTests(e:*):void
		{
			controlSocket.connect();
			socketReporter.runTests(this.tests);
		}

		/**
		 * Test to be run
		 * @param test the Test to run.
		 */
		public function addTest( test:Class ) : void
    	{
    	    tests.push(test);
    	    trace("Testing "+test);
		}
	}
}