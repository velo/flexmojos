package org.sonatype.flexmojos.tests.concept;

import org.testng.annotations.Test;

public class GeneratorTest
    extends AbstractConceptTest
{

    @Test( groups = { "generator" } )
    public void testGenerationGranite1()
        throws Exception
    {
        standardConceptTester( "simple-generation", "-DgeneratorToUse=graniteds1" );
    }

    @Test( groups = { "generator" } )
    public void testGenerationGranite2()
        throws Exception
    {
        standardConceptTester( "simple-generation", "-DgeneratorToUse=graniteds2" );
    }

}
