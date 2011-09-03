package org.sonatype.flexmojos.tests.concept;

import java.io.File;

import org.sonatype.flexmojos.test.FMVerifier;
import org.testng.annotations.Test;

public class DowngradeTest
    extends AbstractConceptTest
{

    @Test
    public void flex2()
        throws Exception
    {
        standardConceptTester( "downgrade-sdk2" );
    }

    @Test( dataProvider = "flex3" )
    public void flex3( String fdk )
        throws Exception
    {
        standardConceptTester( "downgrade-sdk3", fdk );
    }

    @Test( dataProvider = "flex4" )
    public void flex4( String fdk )
        throws Exception
    {
        standardConceptTester( "downgrade-sdk4", fdk );
    }

    public FMVerifier standardConceptTester( String conceptName, String fdk )
        throws Exception
    {
        String projectName = "/concept/" + conceptName;
        File testDir = getProjectCustom( projectName, projectName + "_" + getTestName() + "_" + fdk );
        return test( testDir, "install", "-Dfdk=" + fdk );
    }

}
