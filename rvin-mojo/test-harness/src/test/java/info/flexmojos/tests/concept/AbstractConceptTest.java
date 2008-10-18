package info.flexmojos.tests.concept;

import info.flexmojos.tests.AbstractFlexMojosTests;

import java.io.File;

public abstract class AbstractConceptTest
    extends AbstractFlexMojosTests
{

    public void standardConceptTester( String conceptName )
        throws Exception
    {
        File testDir = getProject( "/concept/" + conceptName );
        test( testDir, "install" );
    }

}
