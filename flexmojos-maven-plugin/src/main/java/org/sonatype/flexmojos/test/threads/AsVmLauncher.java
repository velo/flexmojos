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
package org.sonatype.flexmojos.test.threads;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.StreamPumper;
import org.sonatype.flexmojos.utilities.MavenUtils;

/**
 * ActionScript runtime launcher. This class is used to launch the application that runs unit tests.
 */
@Component( role = AsVmLauncher.class, instantiationStrategy = "per-lookup" )
public class AsVmLauncher
    extends AbstractLogEnabled
    implements ControlledThread
{

    public static final String ROLE = AsVmLauncher.class.getName();

    private static final String WINDOWS_CMD = "FlashPlayer.exe";

    private static final String MAC_CMD = "Flash Player";

    private static final String UNIX_CMD = "flashplayer";

    private ThreadStatus status;

    private boolean holdStatus;

    private Error error;

    private Process process;

    private String getPlatformDefaultCommand()
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
     * @param targetSwf the SWF.
     * @throws IOException
     * @throws Exception if there is an error launching the tests.
     */
    public void init( String flashPlayer, File targetSwf, boolean allowHeadlessMode )
        throws MojoExecutionException
    {
        if ( targetSwf == null )
        {
            throw new MojoExecutionException( "Target SWF not defined" );
        }

        if ( !targetSwf.isFile() )
        {
            throw new MojoExecutionException( "Target SWF not found " + targetSwf );
        }

        if ( flashPlayer == null )
        {
            flashPlayer = getPlatformDefaultCommand();
        }

        setStatus( ThreadStatus.RUNNING );

        getLogger().debug( "[LAUNCHER] ASVmLauncher starting" );

        
        getLogger().debug( "[LAUNCHER] exec: " + flashPlayer + " - " + targetSwf );

        getLogger().debug( "[LAUNCHER] Creating process" );
        if ( allowHeadlessMode && MavenUtils.isLinux() && GraphicsEnvironment.isHeadless() )
        {
            runFlashplayerHeadless( flashPlayer, targetSwf );
        }
        else
        {
            runFlashplayer( flashPlayer, targetSwf );
        }
        getLogger().debug( "[LAUNCHER] Process created " + process );
    }

    private void runFlashplayer( String flashPlayer, File targetSwf )
        throws MojoExecutionException
    {
        try
        {
            process = Runtime.getRuntime().exec( new String[] { flashPlayer, targetSwf.getAbsolutePath() } );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException(
                                              "Failed to launch Flash Player.  Make sure flashplayer is available on PATH"
                                                  + "\n\t\tor use -DflashPlayer.command=${flashplayer executable}"
                                                  + "\nRead more at: https://docs.sonatype.org/display/FLEXMOJOS/Running+unit+tests",
                                              e );
        }
    }

    private void runFlashplayerHeadless( String flashPlayer, File targetSwf )
        throws MojoExecutionException
    {
        getLogger().warn( "[LAUNCHER] Using xvfb-run to launch headless tests" );
        try
        {
            process = Runtime.getRuntime().exec( new String[] { "xvfb-run", flashPlayer, targetSwf.getAbsolutePath() } );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException(
                                              "Failed to launch Flash Player in headless environment.  Make sure flashplayer and xvfb-run are available on PATH"
                                                  + "\n\t\tor use -DflashPlayer.command=${flashplayer executable}"
                                                  + "\nRead more at: https://docs.sonatype.org/display/FLEXMOJOS/Running+unit+tests",
                                              e );
        }
    }

    public void run()
    {

        StreamConsumer stdout = new StreamConsumer()
        {
            public void consumeLine( String line )
            {
                getLogger().debug( "[SYSOUT]: " + line );
            }
        };

        StreamConsumer stderr = new StreamConsumer()
        {
            public void consumeLine( String line )
            {
                getLogger().debug( "[SYSERR]: " + line );
            }
        };

        StreamPumper outputPumper = new StreamPumper( process.getInputStream(), stdout );

        StreamPumper errorPumper = new StreamPumper( process.getErrorStream(), stderr );

        outputPumper.start();

        errorPumper.start();

        getLogger().debug( "[LAUNCHER] Output pumpers ON" );

        int returnCode;
        try
        {
            getLogger().debug( "[LAUNCHER] Waiting for flashplayer termination" );
            returnCode = process.waitFor();
        	getLogger().debug( "[LAUNCHER] Flashplayer closed" );
        }
        catch ( InterruptedException e )
        {
            setStatus( ThreadStatus.ERROR );
            error = new Error( "Error while executing external command, process killed.", e );
            throw error;
        }

        if ( returnCode != 0 )
        {
            setStatus( ThreadStatus.ERROR );
            error = new Error( "Unexpected return code " + returnCode );
        }
        else
        {
            setStatus( ThreadStatus.DONE );
        }
    }

    public void setStatus( ThreadStatus status )
    {
        if ( !holdStatus )
        {
            this.status = status;
        }
    }

    public void setError( Error error )
    {
        this.error = error;
    }

    public void stop()
    {
        holdStatus = true;

        if ( process != null )
        {
        	getLogger().debug( "[LAUNCHER] process has not been finished, destroying" );
        	process.destroy();
        }
    }

    public ThreadStatus getStatus()
    {
        return status;
    }

    public Error getError()
    {
        return this.error;
    }

}
