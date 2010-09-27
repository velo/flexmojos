package org.sonatype.flexmojos.tests.concept;

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
        standardConceptTester( "simplify-air" );
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

        standardConceptTester( "native-air", "-Dos.packages=" + osPackages );
    }
}
