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
package org.sonatype.flexmojos.test.util;


import org.sonatype.flexmojos.test.report.TestCaseReport;
import org.sonatype.flexmojos.test.util.XStreamFactory;
import org.testng.annotations.Test;

import com.thoughtworks.xstream.XStream;

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
    {
        XStream xs = XStreamFactory.getXStreamInstance();
        TestCaseReport report = (TestCaseReport) xs.fromXML( XML );
        System.out.println( report );
    }
}
