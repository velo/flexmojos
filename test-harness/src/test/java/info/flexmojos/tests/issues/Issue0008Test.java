package info.flexmojos.tests.issues;

import org.testng.annotations.Test;

public class Issue0008Test
    extends AbstractIssueTest
{

    @Test( timeOut = 120000 )
    public void issue8part1()
        throws Exception
    {
        testIssue( "issue-0008-1" );
    }

    @Test( timeOut = 120000 )
    public void issue8part2()
        throws Exception
    {
        testIssue( "issue-0008-2" );
    }

}
