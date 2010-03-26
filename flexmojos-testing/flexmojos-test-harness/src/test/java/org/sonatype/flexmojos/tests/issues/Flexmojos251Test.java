package org.sonatype.flexmojos.tests.issues;

import java.io.File;

import org.testng.annotations.Test;

public class Flexmojos251Test
    extends AbstractIssueTest
{

    @Test
    public void verifyHtmlWrapper()
        throws Exception
    {
        File testDir = getProject( "/issues/flexmojos-251" );
        test( testDir, "verify" );
    }

}
