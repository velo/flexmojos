package org.sonatype.flexmojos.tests.issues;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;

import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.sonatype.flexmojos.test.FMVerifier;
import org.sonatype.flexmojos.test.report.TestCaseReport;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class Flexmojos395Test
    extends AbstractIssueTest
{

    @Test
    public void testFlexUnit4Example()
        throws Exception
    {
        FMVerifier v = testIssue( "flexmojos-395" );
        File testDir = new File(v.getBasedir());

        File target = new File( testDir, "target" );
        File sureFireReports = new File( target, "surefire-reports" );
        AssertJUnit.assertTrue( "Report folder not created!", sureFireReports.isDirectory() );

        String[] reportFiles = sureFireReports.list();
        AssertJUnit.assertEquals( "Expected for 1 files, got: " + Arrays.toString( reportFiles ), 1, reportFiles.length );

        File reportFile = new File( sureFireReports, "TEST-AnnotatedTest.addition.AnnotatedTest.xml" );
        AssertJUnit.assertTrue( "Report was not created!", reportFile.isFile() );

        TestCaseReport report = new TestCaseReport( Xpp3DomBuilder.build( new FileReader( reportFile ) ) );

        AssertJUnit.assertEquals( 0, report.getErrors() );
        AssertJUnit.assertEquals( 0, report.getFailures() );
        AssertJUnit.assertEquals( 1, report.getTests() );
    }

}
