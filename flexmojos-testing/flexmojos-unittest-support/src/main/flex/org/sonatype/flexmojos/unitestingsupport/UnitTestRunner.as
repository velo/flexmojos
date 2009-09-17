/**
 * @author Marvin Froeder
 */
package org.sonatype.flexmojos.unitestingsupport {
	
	public interface UnitTestRunner {

		function run(tests:Array):int;
		
		function set socketReporter(socketReporter:SocketReporter):void;
				
	}

}