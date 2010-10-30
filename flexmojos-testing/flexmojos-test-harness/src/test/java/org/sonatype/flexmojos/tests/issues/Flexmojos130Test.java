package org.sonatype.flexmojos.tests.issues;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.util.zip.ZipFile;

import org.sonatype.flexmojos.matcher.file.FileMatcher;
import org.testng.annotations.Test;

public class Flexmojos130Test
    extends AbstractIssueTest
{

    @Test
    public void attachAsdoc()
        throws Exception
    {
        File testDir = getProject( "/issues/flexmojos-130" );
        test( testDir, "install" );

        File swc = new File( testDir, "target/flexmojos-130-1.0-SNAPSHOT.swc" );
        assertThat( swc, FileMatcher.exists() );

        ZipFile swcC = new ZipFile( swc );
        try
        {
            assertNotNull( swcC.getEntry( "docs/ASDoc_Config.xml" ) );
            assertNotNull( swcC.getEntry( "docs/org.sonatype.flexmojos.it.xml" ) );
            assertNotNull( swcC.getEntry( "docs/overviews.xml" ) );
            assertNotNull( swcC.getEntry( "docs/packages.dita" ) );
            assertNotNull( swcC.getEntry( "docs/__Global__.xml" ) );
        }
        finally
        {
            swcC.close();
        }
    }
}
