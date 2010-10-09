package org.sonatype.flexmojos.tests.concept;

import org.testng.annotations.Test;

public class GeneratorTest
    extends AbstractConceptTest
{

    @Test( groups = { "generator" } )
    public void testGenerationGranite1()
        throws Exception
    {
        standardConceptTester( "simple-generation", "-DgeneratorArtifact=flexmojos-generator-graniteds-1.1.0",
                               "-DgeneratorToUse=graniteds1" );
    }

    @Test( groups = { "generator" } )
    public void testGenerationGranite2()
        throws Exception
    {
        standardConceptTester( "simple-generation", "-DgeneratorArtifact=flexmojos-generator-graniteds-2.0.0",
                               "-DgeneratorToUse=graniteds2" );
    }

    @Test( groups = { "generator" } )
    public void testGenerationGranite21()
        throws Exception
    {
        standardConceptTester( "simple-generation", "-DgeneratorArtifact=flexmojos-generator-graniteds-2.1.0",
                               "-DgeneratorToUse=graniteds21" );
    }
    
    @Test( groups = { "generator" } )
    public void testGenerationGranite22()
    throws Exception
    {
        standardConceptTester( "simple-generation", "-DgeneratorArtifact=flexmojos-generator-graniteds-2.2.0",
        "-DgeneratorToUse=graniteds22" );
    }

}
