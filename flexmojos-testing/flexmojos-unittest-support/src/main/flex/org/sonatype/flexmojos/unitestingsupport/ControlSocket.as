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
    import flash.system.fscommand;

	
	public class ControlSocket
	{

	    public static const STATUS:String = "Server Status\n";
	
	    public static const OK:String = "OK\n";
	    
	    public static const FINISHED:String = "FINISHED\n";

		[Inspectable]
		public var port:uint = 1024;
		
		[Inspectable]
		public var server:String = "127.0.0.1";
		
    	private var socket:Socket;

    	private var closeController:CloseController = CloseController.getInstance();

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
						
			var data:String = socket.readUTFBytes( socket.bytesAvailable );
			trace("Data handler received data: " + data);
			

			if ( data == STATUS )
			{
				
				if ( closeController.canClose ) {
					trace("Replying FINISHED");
					socket.writeUTFBytes( FINISHED );
					socket.flush();
					
					// Close the socket.
					if(socket) {
						socket.close();
					}
					
					//Exiting
					
					trace("Exiting");
					fscommand("quit");
						
				} else {
					trace("Replying OK");
					socket.writeUTFBytes( OK );
					socket.flush();
				}
				
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