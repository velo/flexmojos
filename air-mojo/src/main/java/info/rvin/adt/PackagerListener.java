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
