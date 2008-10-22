package info.flexmojos.tests.issues;

import java.io.File;

import org.junit.Test;

public class Issue0106Test
    extends AbstractIssueTest
{

    @Test
    public void issue106()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0106" );
        test( testDir, "asdoc:asdoc" );
    }

}
