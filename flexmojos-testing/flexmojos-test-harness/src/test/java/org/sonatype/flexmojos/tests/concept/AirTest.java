package org.sonatype.flexmojos.tests.concept;

import org.apache.maven.it.Verifier;
import org.sonatype.flexmojos.util.OSUtils;
import org.testng.annotations.Test;

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
        Verifier v = standardConceptTester( "simplify-air" );

        v.assertArtifactPresent( "info.rvin.itest", "simplify-air", "1.0-SNAPSHOT", "pom" );
        v.assertArtifactPresent( "info.rvin.itest", "simplify-air", "1.0-SNAPSHOT", "swf" );
        v.assertArtifactPresent( "info.rvin.itest", "simplify-air", "1.0-SNAPSHOT", "air" );
    }

    @Test
    public void nativeAir()
        throws Exception
    {
        String osPackages;
        if ( OSUtils.isLinux() )
        {
            osPackages = "deb";
        }
        else if ( OSUtils.isMacOS() )
        {
            osPackages = "dmg";
        }
        else
        {
            osPackages = "exe";
        }

        Verifier v = standardConceptTester( "native-air", "-Dos.packages=" + osPackages );
        v.assertArtifactPresent( "info.rvin.itest", "native-air", "1.0-SNAPSHOT", "pom" );
        v.assertArtifactPresent( "info.rvin.itest", "native-air", "1.0-SNAPSHOT", "swf" );
        v.assertArtifactPresent( "info.rvin.itest", "native-air", "1.0-SNAPSHOT", "air" );
        v.assertArtifactPresent( "info.rvin.itest", "native-air", "1.0-SNAPSHOT", osPackages );
    }
}
