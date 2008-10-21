package info.rvin.flexmojo.test;

import info.rvin.flexmojos.utilities.MavenUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import flash.util.StringUtils;

/**
 * This class is used to launch the FlexUnit tests.
 */
public class FlexUnitLauncher
{
    private static final String[] WINDOWS_CMD = new String[] { "rundll32", "url.dll,FileProtocolHandler" };

    private static final String[] MAC_CMD = new String[] { "open", "-g" };

    private static final String[] UNIX_CMD = new String[] { "xdg-open" };

    private String[] launcherCommand = new String[] {};

    public FlexUnitLauncher()
    {
        launcherCommand = getPlatformDefaultCommand();
    }

    public FlexUnitLauncher( List<String> command )
    {
        if ( command != null )
        {
            launcherCommand = command.toArray( new String[] {} );
        }
        else
        {
            launcherCommand = getPlatformDefaultCommand();
        }
    }

    private String[] getPlatformDefaultCommand()
    {
        if ( MavenUtils.isWindows() )
        {
            // Ideally we want to launch the SWF in the player so we can close
            // it, not so easy in a browser. We let 'rundll32' do the work based
            // on the extension of the file passed in.
            return WINDOWS_CMD;
        }
        else if ( MavenUtils.isMac() )
        {
            // Ideally we want to launch the SWF in the player so we can close
            // it, not so easy in a browser. We let 'open' do the work based
            // on the extension of the file passed in.
            return MAC_CMD;
        }
        else
        {
            // If we are running in UNIX the fallback is to the browser. To do
            // this Netscape must be running for the "-remote" flag to work. If
            // the browser is not running we need to start it.
            return UNIX_CMD;
        }
    }

    /**
     * Run the SWF that contains the FlexUnit tests.
     * 
     * @param swf the SWF.
     * @throws Exception if there is an error launching the tests.
     */
    public void runTests( File swf )
        throws Exception
    {
        System.err.println( "runtests: " + Arrays.asList( launcherCommand ) );
        if ( launcherCommand != null )
        {
            launcherCommand = StringUtils.concat( launcherCommand, new String[] { swf.getAbsolutePath() } );
            System.err.println( "exec: " + Arrays.asList( launcherCommand ) );
            Runtime.getRuntime().exec( launcherCommand );
        }
        else if ( MavenUtils.isWindows() )
        {
            // Ideally we want to launch the SWF in the player so we can close
            // it, not so easy in a browser. We let 'rundll32' do the work based
            // on the extension of the file passed in.
            Runtime.getRuntime().exec( StringUtils.concat( WINDOWS_CMD, new String[] { swf.getAbsolutePath() } ) );
        }
        else if ( MavenUtils.isMac() )
        {
            // Ideally we want to launch the SWF in the player so we can close
            // it, not so easy in a browser. We let 'open' do the work based
            // on the extension of the file passed in.
            Runtime.getRuntime().exec( StringUtils.concat( MAC_CMD, new String[] { swf.getAbsolutePath() } ) );
        }
        else
        {
            // If we are running in UNIX the fallback is to the browser. To do
            // this Netscape must be running for the "-remote" flag to work. If
            // the browser is not running we need to start it.
            Process p =
                Runtime.getRuntime().exec( StringUtils.concat( UNIX_CMD, new String[] { swf.getAbsolutePath() } ) );

            // If the exist code is '0', then the browser was running, otherwise
            // we need to start the browser.
            int exitValue = p.waitFor();

            if ( exitValue != 0 )
            {
                Runtime.getRuntime().exec( StringUtils.concat( UNIX_CMD, new String[] { swf.getAbsolutePath() } ) );
            }
        }
    }

}