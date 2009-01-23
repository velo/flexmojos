package org.sonatype.flexmojos.tests.concept;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CopyMojoTest
    extends AbstractConceptTest
{

    @Test( timeOut = 120000 )
    public void copyFlexResouces()
        throws Exception
    {
        File testDir = getProject( "/concept/copy-flex-resources" );
        test( testDir, "install" );

        File warFile = new File( testDir, "war/target/copy-war-1.0-SNAPSHOT.war" );
        Assert.assertTrue( warFile.exists(), "War file not found!" );

        ZipFile war = new ZipFile( warFile );
        ZipEntry swf = war.getEntry( "copy-swf-1.0-SNAPSHOT.swf" );
        Assert.assertNotNull( swf, "Swf entry not present at war!" );
        ZipEntry rsl = war.getEntry( "rsl/framework-3.2.0.3958.swf" );
        Assert.assertNotNull( rsl, "Rsl entry not present at war!" );
    }

}
