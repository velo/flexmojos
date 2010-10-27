package org.sonatype.flexmojos.tests.concept;

import java.io.File;

import org.sonatype.flexmojos.test.FMVerifier;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DowngradeTest
    extends AbstractConceptTest
{

    @DataProvider( name = "flex3" )
    public Object[][] flex3()
    {
        return new Object[][] { { "3.0.0.477" }, { "3.1.0.2710" }, { "3.2.0.3958" }, { "3.3.0.4852" },
            { "3.4.0.9271" }, { "3.5.a.12683" }, { "3.6.0.16321" } };
    }

    @DataProvider( name = "flex4" )
    public Object[][] flex4()
    {
        return new Object[][] { { "4.0.0.14159" }, { "4.1.0.16076" }, { "4.5.0.17855" } , { "4.5.0.17689" } };
    }

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
