package org.sonatype.flexmojos.tests.issues;

import org.testng.annotations.Test;

public class Flexmojos333Test
    extends AbstractIssueTest
{

    @Test
    public void multipleRslUrls()
        throws Exception
    {
        testIssue( "flexmojos-333" );
    }

}
