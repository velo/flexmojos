package org.sonatype.flexmojos.tests.concept;

import org.sonatype.flexmojos.test.FMVerifier;
import org.testng.annotations.Test;

import java.util.zip.ZipFile;

import static org.testng.Assert.assertNotNull;

public class AirTest
    extends AbstractConceptTest
{

    @Test
    public void airApp()
        throws Exception
    {
        standardConceptTester( "simple-air" );
    }

    @Test
    public void simplify()
        throws Exception
    {
        FMVerifier v = standardConceptTester( "simplify-air" );

        v.assertArtifactPresent( "info.rvin.itest", "simplify-air", "1.0-SNAPSHOT", "pom" );
        v.assertArtifactPresent( "info.rvin.itest", "simplify-air", "1.0-SNAPSHOT", "swf" );
        v.assertArtifactPresent( "info.rvin.itest", "simplify-air", "1.0-SNAPSHOT", "air" );

        ZipFile zf =
            new ZipFile( FMVerifier.getArtifactPath( "info.rvin.itest", "simplify-air", "1.0-SNAPSHOT", "air" ) );
        try
        {
            assertNotNull( zf.getEntry( "16.png" ) );
            assertNotNull( zf.getEntry( "32.png" ) );
            assertNotNull( zf.getEntry( "48.png" ) );
            assertNotNull( zf.getEntry( "128.png" ) );
        }
        finally
        {
            zf.close();
        }
    }

    @Test
    public void nativeAir()
        throws Exception
    {
        // String osPackages;
        // if ( OSUtils.isLinux() )
        // {
        // osPackages = "deb";
        // }
        // else if ( OSUtils.isMacOS() )
        // {
        // osPackages = "dmg";
        // }
        // else
        // {
        // osPackages = "exe";
        // }

        FMVerifier v = standardConceptTester( "native-air"/* , "-Dos.packages=" + osPackages */);
        v.assertArtifactPresent( "info.rvin.itest", "native-air", "1.0-SNAPSHOT", "pom" );
        v.assertArtifactPresent( "info.rvin.itest", "native-air", "1.0-SNAPSHOT", "swf" );
        v.assertArtifactPresent( "info.rvin.itest", "native-air", "1.0-SNAPSHOT", "air" );
        // v.assertArtifactPresent( "info.rvin.itest", "native-air", "1.0-SNAPSHOT", "apk" );
        // v.assertArtifactPresent( "info.rvin.itest", "native-air", "1.0-SNAPSHOT", osPackages );
    }

    @Test( dataProvider = "flex3" )
    public void simpleAirFlex3( String fdk )
        throws Exception
    {
        standardConceptTester( "simple-air-sdk3", "-Dfdk=" + fdk );
    }

}
