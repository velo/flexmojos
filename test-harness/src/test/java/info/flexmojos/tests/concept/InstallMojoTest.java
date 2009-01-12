/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
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
    @Test( timeOut = 120000 )
    public void installFake()
        throws Exception
    {
        File testDir = getProject( "/concept/install-sdk" );
        File sdkDir = new File( testDir, "fake-sdk" );

        Verifier verifier = getVerifier( testDir );
        verifier.setAutoclean( false );
        verifier.getCliOptions().add( "-Dflex.sdk.folder=" + sdkDir.getAbsolutePath() );
        verifier.getCliOptions().add( "-Dversion=1.0.0-fake" );
        verifier.executeGoal( "info.flex-mojos:flex-maven-plugin:" + getProperty( "version" ) + ":install-sdk" );
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
