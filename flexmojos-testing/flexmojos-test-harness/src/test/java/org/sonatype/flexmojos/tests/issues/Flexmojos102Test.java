package org.sonatype.flexmojos.tests.issues;

import org.testng.annotations.Test;

public class Flexmojos102Test
    extends AbstractIssueTest
{

    @Test
    public void warWrapper()
        throws Exception
    {
        super.testIssue( "flexmojos-102/war" );
    }

    @Test
    public void pomWrapper()
        throws Exception
    {
        super.testIssue( "flexmojos-102/pom" );
    }
}
