package org.sonatype.flexmojos.tests.issues;

import org.sonatype.flexmojos.test.FMVerifier;
import org.sonatype.flexmojos.tests.AbstractFlexMojosTests;
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
