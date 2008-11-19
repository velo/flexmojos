package info.rvin.flexmojo.test.util;

import info.flexmojos.compile.test.report.TestCaseReport;

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
