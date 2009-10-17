package org.sonatype.flexmojos.tests.concept;

import java.io.File;

import org.apache.maven.it.Verifier;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RSLTest
    extends AbstractConceptTest
{
    @Test
    public void testInternal()
        throws Exception
    {
        Verifier verifier = standardConceptTester( "rsl", "-DconfigurationReport", "-DrslScope=internal" );
        Xpp3Dom config = getFlexConfigReport( verifier, "rsl" );
        Xpp3Dom[] internalLibraries = config.getChild( "compiler" ).getChild( "include-libraries" ).getChildren();
        Assert.assertEquals( 1, internalLibraries.length );
        Assert.assertEquals(
                             new File( internalLibraries[0].getValue() ).getCanonicalPath(),
                             new File( getProperty( "fake-repo" ),
                                       "/com/adobe/flex/framework/spark/4.0.0.8811/spark-4.0.0.8811.swc" ).getCanonicalPath() );

        Assert.assertNull( config.getChild( "runtime-shared-library-path" ) );
    }

    @Test
    public void testMerged()
        throws Exception
    {
        Verifier verifier = standardConceptTester( "rsl", "-DconfigurationReport", "-DrslScope=merged" );
        Xpp3Dom config = getFlexConfigReport( verifier, "rsl" );
        Xpp3Dom compilerConfig = config.getChild( "compiler" );
        Assert.assertNull( config.getChild( "runtime-shared-library-path" ) );
        Assert.assertNull( compilerConfig.getChild( "include-libraries" ) );

        Xpp3Dom[] mergedLibraries = compilerConfig.getChild( "library-path" ).getChildren();

        final String libraryPath =
            new File( getProperty( "fake-repo" ), "/com/adobe/flex/framework/spark/4.0.0.8811/spark-4.0.0.8811.swc" ).getCanonicalPath();
        for ( Xpp3Dom path : mergedLibraries )
        {
            if ( path.getValue().equals( libraryPath ) )
            {
                return;
            }
        }

        Assert.fail( "not found library in merged" );
    }
}
