package org.sonatype.flexmojos.tests.issues;

import org.testng.annotations.Test;

public class Flexmojos167Test
    extends AbstractIssueTest
{

    @Test
    public void linkReportResolve()
        throws Exception
    {
        testIssue( "flexmojos-167" );
    }

}
