package org.sonatype.flexmojos.tests.concept;

import java.io.File;

import org.apache.maven.it.Verifier;
import org.testng.FileAssert;
import org.testng.annotations.Test;

public class OptimizationTest
    extends AbstractConceptTest
{

    @Test( groups = { "optimizer" } )
    public void testOptimizedFlexLibrary()
        throws Exception
    {
        Verifier v = standardConceptTester( "optimized-flex-library" );
        v.assertArtifactPresent( "info.rvin.itest", "optimized-flex-library", "1.0-SNAPSHOT", "swc" );
        v.assertArtifactPresent( "info.rvin.itest", "optimized-flex-library", "1.0-SNAPSHOT", "swf" );
    }

    @Test( groups = { "optimizer" } )
    public void testOptimizedApplication()
        throws Exception
    {
        Verifier v = standardConceptTester( "optimized-application" );
        v.assertArtifactPresent( "info.rvin.itest", "optimized-application", "1.0-SNAPSHOT", "swf" );
        File path = new File( v.getArtifactPath( "info.rvin.itest", "optimized-application", "1.0-SNAPSHOT", "swf" ) );
        FileAssert.assertFile( new File( path.getParentFile(), "optimized-application-1.0-SNAPSHOT-original.swf" ) );
    }

}
