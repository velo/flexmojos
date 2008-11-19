/*
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
package info.flexmojos.compile.test.report {

    [Bindable]
    [RemoteClass(alias="info.flexmojos.compile.test.report.TestMethodReport")]
    public class TestMethodReport extends TestMethodReportBase {
    	
    	/*
  <testcase classname="flex.Test" time="0.297" name="testExecute"/>
  <testcase time="3.125" name="removeAllSnapshots">
    <failure message="All artifacts should be deleted by SnapshotRemoverTask. Found: [H:\home_hudson\.hudson\workspace\Nexus\jdk\1.6\label\windows\trunk\nexus\nexus-test-harness\nexus-test-harness-launcher\target\bundle\nexus-webapp-1.2.0-SNAPSHOT\runtime\work\storage\nexus-test-harness-snapshot-repo\nexus634\artifact\1.0-SNAPSHOT\artifact-1.0-20010101.184024-1.jar, H:\home_hudson\.hudson\workspace\Nexus\jdk\1.6\label\windows\trunk\nexus\nexus-test-harness\nexus-test-harness-launcher\target\bundle\nexus-webapp-1.2.0-SNAPSHOT\runtime\work\storage\nexus-test-harness-snapshot-repo\nexus634\artifact\1.0-SNAPSHOT\artifact-1.0-SNAPSHOT.jar]" type="junit.framework.AssertionFailedError">junit.framework.AssertionFailedError: All artifacts should be deleted by SnapshotRemoverTask. Found: [H:\home_hudson\.hudson\workspace\Nexus\jdk\1.6\label\windows\trunk\nexus\nexus-test-harness\nexus-test-harness-launcher\target\bundle\nexus-webapp-1.2.0-SNAPSHOT\runtime\work\storage\nexus-test-harness-snapshot-repo\nexus634\artifact\1.0-SNAPSHOT\artifact-1.0-20010101.184024-1.jar, H:\home_hudson\.hudson\workspace\Nexus\jdk\1.6\label\windows\trunk\nexus\nexus-test-harness\nexus-test-harness-launcher\target\bundle\nexus-webapp-1.2.0-SNAPSHOT\runtime\work\storage\nexus-test-harness-snapshot-repo\nexus634\artifact\1.0-SNAPSHOT\artifact-1.0-SNAPSHOT.jar]
	at junit.framework.Assert.fail(Assert.java:47)
	at junit.framework.Assert.assertTrue(Assert.java:20)
	</failure>
    <system-out>[INFO] Nexus configuration validated succesfully.
</system-out>
  </testcase>

    	 */
    	public function toXml():XML
    	{
    		var xml:XML =
		    <testcase 
		    	name = { name }
		        time = { time } />;
    			
    		if(error != null) {
				var errorXml:XML =
			      <error
					  message = {error.message } 
			          type = { error.type } >
			        { error.stackTrace }
			      </error>;
 
    			xml = xml.appendChild(errorXml);
    		}

    		if(failure != null) {
				var failureXml:XML =
			      <failure
					  message = {failure.message } 
			          type = { failure.type } >
			        { failure.stackTrace }
			      </failure>;
 
    			xml = xml.appendChild(failureXml);
    		}

    		return xml;
    	}
    }
}