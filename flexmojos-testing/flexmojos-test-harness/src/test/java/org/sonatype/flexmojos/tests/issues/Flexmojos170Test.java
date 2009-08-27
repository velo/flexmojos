package org.sonatype.flexmojos.tests.issues;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Test;

public class Flexmojos170Test
    extends AbstractIssueTest
{

    @Test
    public void generateSwfConfig()
        throws Exception
    {
        File testDir = getProject( "/issues/flexmojos-170/swf" );

        test( testDir, "org.sonatype.flexmojos:flexmojos-maven-plugin:" + getFlexmojosVersion()
            + ":generate-config-swf" );

        checkReport( testDir );
    }

    @Test
    public void generateSwcConfig()
        throws Exception
    {
        File testDir = getProject( "/issues/flexmojos-170/swc" );

        test( testDir, "org.sonatype.flexmojos:flexmojos-maven-plugin:" + getFlexmojosVersion()
            + ":generate-config-swc" );

        checkReport( testDir );
    }

    private void checkReport( File testDir )
    {
        File target = new File( testDir, "target" );
        File configReport = new File( target, "flexmojos-170-1.0-SNAPSHOT-config-report.xml" );
        Assert.assertTrue( configReport.isFile() );
    }

}
