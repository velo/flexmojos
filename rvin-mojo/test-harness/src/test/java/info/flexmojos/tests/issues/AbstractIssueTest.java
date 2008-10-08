package info.flexmojos.tests.issues;

import info.flexmojos.tests.AbstractFlexMojosTests;

import java.io.File;

public abstract class AbstractIssueTest
    extends AbstractFlexMojosTests
{

    public void testIssue( String issueNumber )
        throws Exception
    {
        File testDir = getProject( "/issues/" + issueNumber );
        test( testDir, "install" );
    }

}
