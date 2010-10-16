package org.sonatype.flexmojos.tests.issues;

import java.io.File;
import java.util.Arrays;

import org.sonatype.flexmojos.tests.concept.AbstractConceptTest;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class Flexmojos233Test
    extends AbstractConceptTest
{

    @Test
    public void withSpaces()
        throws Exception
    {
        String projectName = "/concept/flexunit4-example";
        File testDir = getProjectCustom( projectName, projectName + " " + getTestName() );
        test( testDir, "install" );

        File target = new File( testDir, "target" );
        File sureFireReports = new File( target, "surefire-reports" );
        AssertJUnit.assertTrue( "Report folder not created!", sureFireReports.isDirectory() );

        String[] reportFiles = sureFireReports.list();
        AssertJUnit.assertEquals( "Expected for 2 files, got: " + Arrays.toString( reportFiles ), 2, reportFiles.length );
    }

}
