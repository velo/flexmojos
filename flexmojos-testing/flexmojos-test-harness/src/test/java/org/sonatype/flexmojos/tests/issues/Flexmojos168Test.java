package org.sonatype.flexmojos.tests.issues;

import java.io.File;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.flexmojos.plugin.utilities.MavenUtils;
import org.sonatype.flexmojos.test.FMVerifier;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Flexmojos168Test
    extends AbstractIssueTest
{

    @Test
    public void skip()
        throws Exception
    {
        File testDir = getProject( "/issues/flexmojos-168/skip" );
        FMVerifier v = test( testDir, "compile", "-Dflexmojos.skip=true" );

        String log = FileUtils.fileRead( new File( v.getBasedir(), v.getLogFileName() ) );
        Assert.assertTrue( log.contains( "Skipping flexmojos goal execution" ) );
        Assert.assertFalse( log.contains( "Flexmojos " + MavenUtils.getFlexMojosVersion()
            + " - Apache License (NO WARRANTY) - See COPYRIGHT file" ) );

        File target = new File( v.getBasedir(), "target" );
        File swf = new File( target, "flexmojos-168-skip-1.0-SNAPSHOT.swf" );
        Assert.assertFalse( swf.exists() );

        v.assertArtifactNotPresent( "info.rvin.itest.issues", "flexmojos-168-skip", "1.0-SNAPSHOT", "swf" );
    }

    @Test
    public void classifier()
        throws Exception
    {
        FMVerifier v = testIssue( "flexmojos-168/classifier" );

        File target = new File( v.getBasedir(), "target" );
        String filename = "flexmojos-168-1.0-SNAPSHOT-validation.swf";
        File swf = new File( target, filename );
        Assert.assertTrue( swf.exists() );

        File fakeRepo = new File( getProperty( "fake-repo" ) );
        File artifact = new File( fakeRepo, "info/rvin/itest/issues/flexmojos-168/1.0-SNAPSHOT/" + filename );
        Assert.assertTrue( artifact.exists() );
    }

}
