package org.sonatype.flexmojos.tests.concept;

import org.testng.annotations.Test;

public class DowngradeTest
    extends AbstractConceptTest
{

    @Test
    public void flex3()
        throws Exception
    {
        standardConceptTester( "downgrade-sdk" );
    }
}
