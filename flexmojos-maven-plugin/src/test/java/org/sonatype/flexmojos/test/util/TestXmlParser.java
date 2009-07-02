/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.test.util;

import java.io.StringReader;

import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.sonatype.flexmojos.test.report.TestCaseReport;
import org.testng.annotations.Test;

public class TestXmlParser
{

    private static final String XML =
        "<testsuite errors=\"0\" skipped=\"0\" tests=\"1\" time=\"0.312\" failures=\"0\" name=\"com.Test\">"
            + "<testcase classname=\"flex.Test\" time=\"0.297\" name=\"testExecute\"/>"
            + "<testcase classname=\"flex.Test\" time=\"0.297\" name=\"testExecute\"/>"
            + "<testcase time=\"3.125\" name=\"removeAllSnapshots\">"
            + "<failure message=\"All artifacts should be deleted by SnapshotRemoverTask.\" type=\"junit.framework.AssertionFailedError\">"
            + "junit.framework.AssertionFailedError: All artifacts should be deleted by SnapshotRemoverTask."
            + "at junit.framework.Assert.fail(Assert.java:47)" + "at junit.framework.Assert.assertTrue(Assert.java:20)"
            + "</failure>" + "</testcase>" + "</testsuite>";

    @Test
    public void parseXml()
        throws Exception
    {
        TestCaseReport report = new TestCaseReport( Xpp3DomBuilder.build( new StringReader( XML ) ) );
        System.out.println( report );
    }
}
