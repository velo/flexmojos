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

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.StreamPumper;
import org.sonatype.flexmojos.utilities.MavenUtils;

/**
 * ActionScript runtime launcher. This class is used to launch the application that runs unit tests.
 * 
 * @plexus.component role="org.sonatype.flexmojos.test.threads.ASRLauncher"
 */
public class AsVmLauncher
    extends AbstractLogEnabled
    implements ControlledThread
{

    public static final String ROLE = AsVmLauncher.class.getName();

    private static final String WINDOWS_CMD = "FlashPlayer.exe";

    private static final String MAC_CMD = "FlashPlayer.app";

    private static final String UNIX_CMD = "FlashPlayer";

    private ThreadStatus status;

    private boolean holdStatus;

    private Error error;

    private Process process;

    private String flashPlayerCommand;

    private String targetSwf;

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
     * @throws Exception if there is an error launching the tests.
     */
    public void init( String flashPlayer, File targetSwf )
    {
        if ( flashPlayer == null )
        {
            flashPlayer = getPlatformDefaultCommand();
        }

        this.flashPlayerCommand = flashPlayer;
        this.targetSwf = targetSwf.getAbsolutePath();
    }

    public void run()
    {
        if ( flashPlayerCommand == null )
        {
            setStatus( ThreadStatus.ERROR );
            error = new Error( "Command Line not defined, try to initilize FlexUnit Launcher" );
            throw error;
        }
        setStatus( ThreadStatus.RUNNING );

        getLogger().debug( "exec: " + flashPlayerCommand );

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

        try
        {
            getLogger().debug( "Creating process" );
            process = Runtime.getRuntime().exec( new String[] { flashPlayerCommand, targetSwf } );
            getLogger().debug( "Process created " + process );
        }
        catch ( IOException e )
        {
            setStatus( ThreadStatus.ERROR );
            error = new Error( "Error running flashplayer", e );
            throw error;
        }

        StreamPumper outputPumper = new StreamPumper( process.getInputStream(), stdout );

        StreamPumper errorPumper = new StreamPumper( process.getErrorStream(), stderr );

        outputPumper.start();

        errorPumper.start();

        getLogger().debug( "Output pumpers ON" );

        int returnCode;
        try
        {
            getLogger().debug( "Waiting for" );
            returnCode = process.waitFor();
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
        process.destroy();
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
