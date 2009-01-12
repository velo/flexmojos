/**
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
/**
 * 
 */
package info.rvin.adt;

import java.util.ArrayList;
import java.util.List;

import com.adobe.air.Listener;
import com.adobe.air.Message;

/**
 * Collects the messages from the ADT packager tool
 * 
 * @author Joost den Boer
 *
 */
public class MessageCollector implements Listener {

	/**
	 * List of messages.
	 */
	public List<ADTMessage> messages;
	
	/**
	 * Indicates whether messages have been collected.
	 */
	public boolean hasMessages = false;
	
	/**
	 * 
	 */
	public MessageCollector() {
		super();
	}

	/**
	 * Collect messages
	 * 
	 * @see com.adobe.air.Listener#message(com.adobe.air.Message)
	 */
	public void message(Message msg) {

		if(null == messages) {
			messages = new ArrayList<ADTMessage>();
		}
		messages.add(new ADTMessage(msg));
		hasMessages = true;
	}

	/**
	 * @see com.adobe.air.Listener#progress(int, int)
	 */
	public void progress(int i, int j) {
		
		System.out.println(i+" - "+j);
	}
	
	/**
	 * Facade for the real ADT message.
	 * 
	 * @author Joost den Boer
	 *
	 */
	public class ADTMessage {
		
		public int code;
		public int column;
		public String file;
		public int line;
		public String type;
		public String[] identifiers;
		
		public ADTMessage(Message msg) {
			super();
			this.code = msg.code;
			this.column = msg.column;
			this.file = msg.file;
			this.line = msg.line;
			this.type = msg.type;
			this.identifiers = msg.identifiers;
		}
		
		@Override
		public String toString() {

			StringBuilder sb = new StringBuilder();
			sb.append("Message code: ").append(code).append("\n");
			sb.append("Message column: ").append(column).append("\n");
			sb.append("Message file: ").append(file).append("\n");
			sb.append("Message line: ").append(line).append("\n");
			sb.append("Message type: ").append(type).append("\n");
			
			for(String id : identifiers) {
				sb.append("Message identifiers: ").append(id).append("\n");
			}
			return sb.toString();
		}
	}
}
