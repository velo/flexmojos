package org.sonatype.flexmojos.tests.issues;

import org.testng.annotations.Test;

public class Flexmojos166Test
    extends AbstractIssueTest
{

    @Test
    public void cssCompilation()
        throws Exception
    {
        testIssue( "flexmojos-166" );
    }

}
