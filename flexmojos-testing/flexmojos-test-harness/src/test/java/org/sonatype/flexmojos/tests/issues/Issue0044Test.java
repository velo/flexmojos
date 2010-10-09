package org.sonatype.flexmojos.tests.issues;

import java.io.File;

import org.apache.maven.it.VerificationException;
import org.testng.annotations.Test;

public class Issue0044Test
    extends AbstractIssueTest
{

    @Test( expectedExceptions = { VerificationException.class } )
    public void issue44()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0044" );
        test( testDir, "flexmojos:asdoc" );
    }

}
