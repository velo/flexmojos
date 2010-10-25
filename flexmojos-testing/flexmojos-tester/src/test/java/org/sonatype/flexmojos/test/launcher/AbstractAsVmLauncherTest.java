package org.sonatype.flexmojos.test.launcher;

import static org.sonatype.flexmojos.util.PathUtil.file;
import static org.sonatype.flexmojos.util.PathUtil.path;

import java.io.File;
import java.net.URISyntaxException;

import org.codehaus.plexus.PlexusTestNGCase;
import org.sonatype.flexmojos.test.TestRequest;
import org.sonatype.flexmojos.util.OSUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class AbstractAsVmLauncherTest
    extends PlexusTestNGCase
{

    protected AsVmLauncher launcher;

    protected static final TestRequest VALID_SWF;

    protected static final TestRequest INVALID_SWF;

    static
    {
        try
        {
        	VALID_SWF = new TestRequest();
        	VALID_SWF.setSwf( new File( AsVmLauncherTest.class.getResource( "/SelftExit.swf" ).toURI() ) );
        	INVALID_SWF = new TestRequest();
        	INVALID_SWF.setSwf( new File( AsVmLauncherTest.class.getResource( "/NonExit.swf" ).toURI() ) );
        	INVALID_SWF.setFirstConnectionTimeout( 1000 );
        	INVALID_SWF.setTestTimeout( 1000 );
            
        	if ( OSUtils.isWindows() )
            {
                String fp = path( file( "target/flashplayer/flashplayer.exe" ) );
                VALID_SWF.setFlashplayerCommand( new String[] { fp } );
                INVALID_SWF.setFlashplayerCommand( new String[] { fp } );
            }
            else if ( OSUtils.isLinux() )
            {
                String fp = path( file( "target/flashplayer/flashplayer-linux.uexe" ) );
                VALID_SWF.setFlashplayerCommand( new String[] { fp } );
                INVALID_SWF.setFlashplayerCommand( new String[] { fp } );
                file( fp ).setExecutable( true );
            }
            else
            {
            	//do not set the path to flash player, it should use the system default.
            	
            }
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeException();// won't happen, I hope =D
        }
    }

    @BeforeMethod
    public void setUp()
        throws Exception
    {
        launcher = lookup( AsVmLauncher.class );
    }

    @AfterMethod
    public void tearDown()
        throws Exception
    {
        launcher.stop();
    }

}