package org.sonatype.flexmojos.tests.issues;

import org.testng.annotations.Test;

public class Flexmojos252Test
    extends AbstractIssueTest
{

    @Test
    public void spark()
        throws Exception
    {
        testIssue( "flexmojos-252" );
    }
}
