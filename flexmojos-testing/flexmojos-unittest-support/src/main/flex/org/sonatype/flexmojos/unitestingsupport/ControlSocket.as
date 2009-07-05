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

		public function connect():void
		{
			socket = new Socket();
			socket.addEventListener( ProgressEvent.SOCKET_DATA, dataHandler );
			socket.addEventListener( Event.CLOSE, exitFP );
   	   		socket.connect( server, port );
		}
		
		private function exitFP( event:* ):void
		{
			//Exiting
			trace("Exiting");
			fscommand("quit");
		}

		/**
		 * Event listener to handle data received on the socket.
		 * @param event the DataEvent.
		 */
		private function dataHandler( event:* ):void
		{
						
			var data:String = socket.readUTFBytes( socket.bytesAvailable );
			trace("Data handler received data: " + data);
			
			var status:String = CommConstraints.STATUS + CommConstraints.EOL;

			if ( data == status )
			{
				if ( closeController.canClose ) {
					trace("Replying FINISHED");
					socket.writeUTFBytes( CommConstraints.FINISHED + CommConstraints.EOL );
					socket.flush();
				} else {
					trace("Replying OK");
					socket.writeUTFBytes( CommConstraints.OK + CommConstraints.EOL );
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