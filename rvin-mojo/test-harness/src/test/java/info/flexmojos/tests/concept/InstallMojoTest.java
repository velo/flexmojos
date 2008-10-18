package info.flexmojos.tests.concept;

import java.io.File;

import junit.framework.Assert;

import org.apache.maven.it.Verifier;
import org.junit.Test;

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
        Assert.assertTrue( compilerPom.exists() );
        File compilerLibrary =
            new File( repoDir, "com/adobe/flex/compiler/compiler-library/1.0.0-fake/compiler-library-1.0.0-fake.jar" );
        Assert.assertTrue( compilerLibrary.exists() );

        // framework stuff
        File flexFrameworkPom =
            new File( repoDir, "com/adobe/flex/framework/flex-framework/1.0.0-fake/flex-framework-1.0.0-fake.pom" );
        Assert.assertTrue( flexFrameworkPom.exists() );
        File airFrameworkPom =
            new File( repoDir, "com/adobe/flex/framework/air-framework/1.0.0-fake/air-framework-1.0.0-fake.pom" );
        Assert.assertTrue( airFrameworkPom.exists() );
        File flexLibrary =
            new File( repoDir, "com/adobe/flex/framework/flex-library/1.0.0-fake/flex-library-1.0.0-fake.swc" );
        Assert.assertTrue( flexLibrary.exists() );
        File flexLibraryBeaconLocale =
            new File( repoDir, "com/adobe/flex/framework/flex-library/1.0.0-fake/flex-library-1.0.0-fake.rb.swc" );
        Assert.assertTrue( flexLibraryBeaconLocale.exists() );
        File flexLibraryEnUsLocale =
            new File( repoDir, "com/adobe/flex/framework/flex-library/1.0.0-fake/flex-library-1.0.0-fake-en_US.rb.swc" );
        Assert.assertTrue( flexLibraryEnUsLocale.exists() );
    }
}
