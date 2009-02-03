/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.tests.concept;

import java.io.File;
import java.io.IOException;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@Test( sequential = true )
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

    @Test
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

    @Test
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

    // @Test( alwaysRun = true, dependsOnMethods = { "installFramework" } )
    void accidentalOverwriteProtection()
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
            verifier.executeGoal( GROUP_ID + ":" + ARTIFACT_ID + ":" + VERSION + ":" + GOAL );
            verifier.verifyErrorFreeLog();
            AssertJUnit.fail( "Install mojo fail to prevent FDK get overwrote!" );
        }
        catch ( VerificationException e )
        {
            // must happen
        }
        verifier.verifyTextInLog( "never overwrite Flex SDK" );
    }

    private static final String GROUP_ID = "org.sonatype.plugins";

    private static final String ARTIFACT_ID = "bundle-publisher-maven-plugin";

    private static final String GOAL = "install-bundle";

    private static final String VERSION = "1.0-SNAPSHOT";

    private void installSDK( File descriptor )
        throws IOException, VerificationException
    {
        Verifier verifier = getInstallVerifier( descriptor );
        verifier.executeGoal( GROUP_ID + ":" + ARTIFACT_ID + ":" + VERSION + ":" + GOAL );
        verifier.verifyErrorFreeLog();
    }

    @SuppressWarnings( "unchecked" )
    private Verifier getInstallVerifier( File descriptor )
        throws VerificationException
    {
        File sdkBundle = new File( testDir, "flex-sdk-" + getProperty( "flex-version" ) + "-bundle.zip" );
        Verifier verifier = getVerifier( testDir );
        verifier.setAutoclean( false );
        verifier.getCliOptions().add( "-Dbundle=" + sdkBundle.getAbsolutePath() );
        verifier.getCliOptions().add( "-Ddescriptor=" + descriptor.getAbsolutePath() );
        return verifier;
    }

}
