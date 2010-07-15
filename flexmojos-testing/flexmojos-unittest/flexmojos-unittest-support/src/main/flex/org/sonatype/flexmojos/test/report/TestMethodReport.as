/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.test.report {

    [Bindable]
    [RemoteClass(alias="org.sonatype.flexmojos.test.report.TestMethodReport")]
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
    		var genxml:XML =
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
 
    			genxml = genxml.appendChild(errorXml);
    		}

    		if(failure != null) {
				var failureXml:XML =
			      <failure
					  message = {failure.message } 
			          type = { failure.type } >
			        { failure.stackTrace }
			      </failure>;
 
    			genxml = genxml.appendChild(failureXml);
    		}

    		return genxml;
    	}
    }
}