package org.sonatype.flexmojos.tests.concept;

import org.testng.annotations.Test;

public class ManifestTest
    extends AbstractConceptTest
{

    @Test
    public void manifestCreation()
        throws Exception
    {
        standardConceptTester( "manifest" );
    }

}
