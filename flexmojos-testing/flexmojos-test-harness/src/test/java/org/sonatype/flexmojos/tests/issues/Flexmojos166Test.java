package org.sonatype.flexmojos.tests.issues;

import org.testng.annotations.Test;

public class Flexmojos166Test
    extends AbstractIssueTest
{

    @Test
    public void disableDefaultLocale()
        throws Exception
    {
        testIssue( "flexmojos-166" );
    }

}
