package info.flexmojos.tests.issues;

import org.testng.annotations.Test;

public class Issue0008Test
    extends AbstractIssueTest
{

    @Test
    public void issue8part1()
        throws Exception
    {
        testIssue( "issue-0008-1" );
    }

    @Test
    public void issue8part2()
        throws Exception
    {
        testIssue( "issue-0008-2" );
    }

}
