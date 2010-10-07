package org.sonatype.flexmojos.tests.issues;

import java.io.File;

import org.apache.maven.it.VerificationException;
import org.sonatype.flexmojos.test.FMVerifier;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Flexmojos141Test
    extends AbstractIssueTest
{

    @Test
    public void testInvalidVersion()
        throws Exception
    {
        File testDir = getProject( "/issues/" + "flexmojos-141" );
        FMVerifier verifier = getVerifier( testDir );
        try
        {
            verifier.executeGoal( "install" );
            Assert.fail();
        }
        catch ( VerificationException e )
        {
            // expected
        }

        verifier.verifyTextInLog( "Flex compiler and flex framework versions doesn't match." );
    }

}
