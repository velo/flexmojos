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
	import flash.events.ProgressEvent;
	import flash.net.Socket;
	
	public class ControlSocket
	{

	    public static const STATUS:String = "Server Status";
	
	    public static const OK:String = "OK";

		[Inspectable]
		public var port:uint = 1024;
		
		[Inspectable]
		public var server:String = "127.0.0.1";
		
    	private var socket:Socket;

		public function connect():void
		{
			socket = new Socket();
			socket.addEventListener( ProgressEvent.SOCKET_DATA, dataHandler );
   	   		socket.connect( server, port );
		}
		
		/**
		 * Event listener to handle data received on the socket.
		 * @param event the DataEvent.
		 */
		private function dataHandler( event:* ):void
		{
			trace("dataHandler");
			
			var data:String = socket.readUTFBytes( socket.bytesAvailable );
			trace("data " + data);
			

			if ( data == STATUS )
			{
				trace( "replying" );
				socket.writeUTFBytes( OK );
   			}
		}
		
		private static var instance:ControlSocket;
		
		public static function getInstance():ControlSocket {
			if(instance == null) {
				instance = new ControlSocket();
			}
			return instance;
		}

	}
}