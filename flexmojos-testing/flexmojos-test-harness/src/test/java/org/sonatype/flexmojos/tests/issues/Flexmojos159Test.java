package org.sonatype.flexmojos.tests.issues;

import org.testng.annotations.Test;

public class Flexmojos159Test
    extends AbstractIssueTest
{

    @Test
    public void localizationFromLibrary()
        throws Exception
    {
        super.testIssue( "flexmojos-159" );
    }

}
