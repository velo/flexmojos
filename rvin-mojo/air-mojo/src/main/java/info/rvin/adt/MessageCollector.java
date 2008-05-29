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
