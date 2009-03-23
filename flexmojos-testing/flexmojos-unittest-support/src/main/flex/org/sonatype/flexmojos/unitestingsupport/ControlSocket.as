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