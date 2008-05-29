/**
 * 
 */
package info.rvin.adt;

import org.apache.maven.plugin.logging.Log;

import com.adobe.air.Listener;
import com.adobe.air.Message;

/**
 * @author Joost den Boer
 *
 */
public class PackagerListener implements Listener {

	// instance to which log messages
	private Log logger;
	
	/**
	 * 
	 */
	public PackagerListener(Log logger) {
	}

	/**
	 * Log message to logger
	 * 
	 * @see com.adobe.air.Listener#message(com.adobe.air.Message)
	 */
	public void message(Message msg) {
		
		System.out.println("Message code: "+msg.code);
		System.out.println("Message column: "+msg.column);
		System.out.println("Message file: "+msg.file);
		System.out.println("Message line: "+msg.line);
		System.out.println("Message type: "+msg.type);
		
		for(String id : msg.identifiers) {
			System.out.println("Message identifiers: "+id);
		}

		logger.debug("Message code: "+msg.code);
		logger.debug("Message column: "+msg.column);
		logger.debug("Message file: "+msg.file);
		logger.debug("Message line: "+msg.line);
		logger.debug("Message type: "+msg.type);
		logger.debug("Message identifiers: "+msg.identifiers);

	}

	/**
	 * Show progress in logger
	 * 
	 * @see com.adobe.air.Listener#progress(int, int)
	 */
	public void progress(int i, int j) {
		
		System.out.println(i+" - "+j);
	}

}
