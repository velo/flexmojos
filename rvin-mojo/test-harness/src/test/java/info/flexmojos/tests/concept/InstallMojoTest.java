/*
 * Flex-mojos is a set of maven plugins to allow maven users to compile, optimize, test and ... Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008 Marvin Herman Froeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.flexmojos.tests.concept;

import java.io.File;

import org.apache.maven.it.Verifier;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class InstallMojoTest
    extends AbstractConceptTest
{

    @SuppressWarnings( "unchecked" )
    @Test
    public void installFake()
        throws Exception
    {
        File testDir = getProject( "/concept/install-sdk" );
        File sdkDir = new File( testDir, "fake-sdk" );

        Verifier verifier = getVerifier( testDir );
        verifier.setAutoclean( false );
        verifier.getCliOptions().add( "-Dflex.sdk.folder=" + sdkDir.getAbsolutePath() );
        verifier.getCliOptions().add( "-Dversion=1.0.0-fake" );
        verifier.executeGoal( "info.flex-mojos:install-mojo:" + getProperty( "version" ) + ":install-sdk" );
        verifier.verifyErrorFreeLog();

        File repoDir = new File( getProperty( "fake-repo" ) );

        // compiler stuff
        File compilerPom = new File( repoDir, "com/adobe/flex/compiler/1.0.0-fake/compiler-1.0.0-fake.pom" );
        AssertJUnit.assertTrue( compilerPom.exists() );
        File compilerLibrary =
            new File( repoDir, "com/adobe/flex/compiler/compiler-library/1.0.0-fake/compiler-library-1.0.0-fake.jar" );
        AssertJUnit.assertTrue( compilerLibrary.exists() );

        // framework stuff
        File flexFrameworkPom =
            new File( repoDir, "com/adobe/flex/framework/flex-framework/1.0.0-fake/flex-framework-1.0.0-fake.pom" );
        AssertJUnit.assertTrue( flexFrameworkPom.exists() );
        File airFrameworkPom =
            new File( repoDir, "com/adobe/flex/framework/air-framework/1.0.0-fake/air-framework-1.0.0-fake.pom" );
        AssertJUnit.assertTrue( airFrameworkPom.exists() );
        File flexLibrary =
            new File( repoDir, "com/adobe/flex/framework/flex-library/1.0.0-fake/flex-library-1.0.0-fake.swc" );
        AssertJUnit.assertTrue( flexLibrary.exists() );
        File flexLibraryBeaconLocale =
            new File( repoDir, "com/adobe/flex/framework/flex-library/1.0.0-fake/flex-library-1.0.0-fake.rb.swc" );
        AssertJUnit.assertTrue( flexLibraryBeaconLocale.exists() );
        File flexLibraryEnUsLocale =
            new File( repoDir, "com/adobe/flex/framework/flex-library/1.0.0-fake/flex-library-1.0.0-fake-en_US.rb.swc" );
        AssertJUnit.assertTrue( flexLibraryEnUsLocale.exists() );
    }
}
