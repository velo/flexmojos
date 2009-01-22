/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sonatype.flexmojos.tests.concept;

import java.io.File;
import java.io.IOException;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class InstallMojoTest
    extends AbstractConceptTest
{

    private File testDir;

    @BeforeTest
    public void init()
        throws IOException
    {
        testDir = getProject( "/concept/install-sdk" );
    }

    @Test( timeOut = 120000 )
    public void installCompiler()
        throws Exception
    {
        File compilerDescriptor = new File( testDir, "compiler-descriptor.xml" );

        installSDK( compilerDescriptor );

        File repoDir = new File( getProperty( "fake-repo" ) );

        // compiler stuff
        File compilerPom = new File( repoDir, "com/adobe/flex/compiler/1.0-fake/compiler-1.0-fake.pom" );
        AssertJUnit.assertTrue( compilerPom.exists() );
        File compilerLibrary = new File( repoDir, "com/adobe/flex/compiler/asdoc/1.0-fake/asdoc-1.0-fake.jar" );
        AssertJUnit.assertTrue( compilerLibrary.exists() );
    }

    @Test( timeOut = 120000 )
    public void installFramework()
        throws Exception
    {
        File frameworkDescriptor = new File( testDir, "flex-descriptor.xml" );

        installSDK( frameworkDescriptor );

        File repoDir = new File( getProperty( "fake-repo" ) );

        // framework stuff
        File flexFrameworkPom =
            new File( repoDir, "com/adobe/flex/framework/flex-framework/1.0-fake/flex-framework-1.0-fake.pom" );
        AssertJUnit.assertTrue( flexFrameworkPom.exists() );
        File airFrameworkPom =
            new File( repoDir, "com/adobe/flex/framework/air-framework/1.0-fake/air-framework-1.0-fake.pom" );
        AssertJUnit.assertTrue( airFrameworkPom.exists() );
        File flexLibrary =
            new File( repoDir, "com/adobe/flex/framework/airframework/1.0-fake/airframework-1.0-fake.swc" );
        AssertJUnit.assertTrue( flexLibrary.exists() );
        File flexLibraryBeaconLocale =
            new File( repoDir, "com/adobe/flex/framework/airframework/1.0-fake/airframework-1.0-fake-en_US.rb.swc" );
        AssertJUnit.assertTrue( flexLibraryBeaconLocale.exists() );
        File flexLibraryEnUsLocale =
            new File( repoDir, "com/adobe/flex/framework/airframework/1.0-fake/airframework-1.0-fake.pom" );
        AssertJUnit.assertTrue( flexLibraryEnUsLocale.exists() );
    }

    @Test( timeOut = 120000 )
    public void accidentalOverwriteProtection()
        throws Exception
    {
        File frameworkDescriptor = new File( testDir, "flex-descriptor.xml" );

        try
        {
            installSDK( frameworkDescriptor );
        }
        catch ( VerificationException e )
        {
            // can happen
        }

        Verifier verifier = getInstallVerifier( frameworkDescriptor );
        try
        {
            verifier.executeGoal( "org.sonatype.flexmojos:flex-maven-plugin:" + getProperty( "version" )
                + ":install-sdk" );
            verifier.verifyErrorFreeLog();
            AssertJUnit.fail( "Install mojo fail to prevent FDK get overwrote!" );
        }
        catch ( VerificationException e )
        {
            // must happen
        }
        verifier.verifyTextInLog( "never overwrite Flex SDK" );
    }

    private void installSDK( File descriptor )
        throws IOException, VerificationException
    {
        Verifier verifier = getInstallVerifier( descriptor );
        verifier.executeGoal( "org.sonatype.flexmojos:flex-maven-plugin:" + getProperty( "version" ) + ":install-sdk" );
        verifier.verifyErrorFreeLog();
    }

    @SuppressWarnings( "unchecked" )
    private Verifier getInstallVerifier( File descriptor )
        throws VerificationException
    {
        File sdkBundle = new File( testDir, "flex-sdk-" + getProperty( "flex-version" ) + "-bundle.zip" );
        Verifier verifier = getVerifier( testDir );
        verifier.setAutoclean( false );
        verifier.getCliOptions().add( "-Dflex.sdk.bundle=" + sdkBundle.getAbsolutePath() );
        verifier.getCliOptions().add( "-Dflex.sdk.descriptor=" + descriptor.getAbsolutePath() );
        return verifier;
    }

}
