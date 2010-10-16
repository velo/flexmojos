package org.sonatype.flexmojos.tests.issues;

import static org.testng.Assert.fail;

import org.apache.maven.it.VerificationException;
import org.sonatype.flexmojos.test.FMVerifier;
import org.sonatype.flexmojos.tests.AbstractFlexMojosTests;
import org.testng.annotations.Test;

public class Flexmojos2Test
    extends AbstractFlexMojosTests
{

    @Test
    public void allFilesUsed()
        throws Exception
    {
        FMVerifier v = test( getProject( "intro/hello-world" ), "install", "-Dflex.failIfUnused=true" );
        v.verifyTextInLog( "All files included" );
    }

    @Test
    public void notAllUsed()
        throws Exception
    {
        FMVerifier v = getVerifier( getProject( "issues/flexmojos-2/simple" ) );
        v.getCliOptions().add( "-Dflex.failIfUnused=true" );
        try
        {
            v.executeGoal( "install" );
            fail( "there was supposed to be unsued files" );
        }
        catch ( VerificationException e )
        {
            v.verifyTextInLog( "Some files were not included on the build:" );
            v.verifyTextInLog( "module.mxml" );
        }
    }

    @Test
    public void modules()
        throws Exception
    {
        FMVerifier v = test( getProject( "issues/flexmojos-2/modules" ), "install", "-Dflex.failIfUnused=true" );
        v.verifyTextInLog( "All files included" );
    }

}
