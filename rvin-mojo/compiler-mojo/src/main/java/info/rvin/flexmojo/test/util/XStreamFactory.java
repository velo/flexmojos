package info.rvin.flexmojo.test.util;

import info.flexmojos.compile.test.report.ErrorReport;
import info.flexmojos.compile.test.report.TestCaseReport;
import info.flexmojos.compile.test.report.TestMethodReport;

import com.thoughtworks.xstream.XStream;

public class XStreamFactory
{
    public static XStream getXStreamInstance()
    {
        XStream xs = new XStream();
        xs.processAnnotations( TestCaseReport.class );
        xs.processAnnotations( TestMethodReport.class );
        xs.processAnnotations( ErrorReport.class );
        return xs;
    }

}
