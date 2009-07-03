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
package org.sonatype.flexmojos.test.launcher;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.StreamPumper;
import org.sonatype.flexmojos.test.AbstractControlledThread;
import org.sonatype.flexmojos.test.ControlledThread;
import org.sonatype.flexmojos.test.ThreadStatus;
import org.sonatype.flexmojos.test.util.OSUtils;

/**
 * ActionScript runtime launcher. This class is used to launch the application that runs unit tests.
 */
@Component( role = AsVmLauncher.class, instantiationStrategy = "per-lookup" )
public class AsVmLauncher
    extends AbstractControlledThread
    implements ControlledThread
{

    private Process process;

    @Configuration( value = "true" )
    private boolean allowHeadlessMode;

    @Configuration( value = "${flashplayer.command}" )
    private String flashplayerCommand;

    private File log;

    /**
     * Run the SWF that contains the FlexUnit tests.
     * 
     * @param targetSwf the SWF.
     * @throws LaunchFlashPlayerException
     */
    public void start( File targetSwf )
        throws LaunchFlashPlayerException, InvalidSwfException
    {
        getLogger().setThreshold( Logger.LEVEL_DEBUG );

        if ( targetSwf == null )
        {
            throw new InvalidSwfException( "targetSwf is null" );
        }

        if ( !targetSwf.exists() )
        {
            throw new InvalidSwfException( "targetSwf not found " + targetSwf );
        }

        if ( flashplayerCommand == null || "${flashplayer.command}".equals( flashplayerCommand ) )
        {
            flashplayerCommand = OSUtils.getPlatformDefaultCommand();
        }

        getLogger().debug( "[LAUNCHER] ASVmLauncher starting" );

        getLogger().debug( "[LAUNCHER] exec: " + flashplayerCommand + " - " + targetSwf );

        getLogger().debug( "[LAUNCHER] Creating process" );
        if ( useXvfb() )
        {
            runFlashplayerHeadless( flashplayerCommand, targetSwf );
        }
        else
        {
            runFlashplayer( flashplayerCommand, targetSwf );
        }
        getLogger().debug( "[LAUNCHER] Process created " + process );

        status = ThreadStatus.STARTED;

        launch();
    }

    private boolean useXvfb()
    {
        return allowHeadlessMode && OSUtils.isLinux() && GraphicsEnvironment.isHeadless();
    }

    private void runFlashplayer( String flashPlayer, File targetSwf )
        throws LaunchFlashPlayerException
    {
        getLogger().warn( "[LAUNCHER] Using regular flashplayer tests" );
        try
        {
            process = Runtime.getRuntime().exec( new String[] { flashPlayer, targetSwf.getAbsolutePath() } );
        }
        catch ( IOException e )
        {
            throw new LaunchFlashPlayerException( "Failed to launch Flash Player.", e );
        }
    }

    private void runFlashplayerHeadless( String flashPlayer, File targetSwf )
        throws LaunchFlashPlayerException
    {
        getLogger().warn( "[LAUNCHER] Using xvfb-run to launch headless tests" );

        try
        {
            FileUtils.forceDelete( "/tmp/.X99-lock" );
            FileUtils.forceDelete( "/tmp/.X11-unix" );
        }
        catch ( IOException e )
        {
            throw new LaunchFlashPlayerException(
                                                  "Failed to delete Xvfb locking files, does the current user has access?",
                                                  e );
        }

        try
        {
            log = File.createTempFile( "xvfbrun", "flashplayer" );
        }
        catch ( IOException e )
        {
            throw new LaunchFlashPlayerException( "Failed to create xvfb-run error-file!", e );
        }

        try
        {
            process =
                Runtime.getRuntime().exec(
                                           new String[] { "xvfb-run", "-a", "-e", log.getAbsolutePath(), flashPlayer,
                                               targetSwf.getAbsolutePath() } );
        }
        catch ( IOException e )
        {
            throw new LaunchFlashPlayerException( "Failed to launch Flash Player in headless environment.", e );
        }
    }

    private StringBuffer consoleLog = new StringBuffer();

    private class ConsoleConsumer
        implements StreamConsumer
    {

        private String prefix;

        public ConsoleConsumer( String prefix )
        {
            this.prefix = prefix;
        }

        public void consumeLine( String line )
        {
            if ( "\n".equals( line ) )
            {
                return;
            }

            getLogger().debug( prefix + line );
            consoleLog.append( prefix ).append( line ).append( '\n' );
        }

    }

    public void run()
    {
        status = ThreadStatus.RUNNING;

        new StreamPumper( process.getInputStream(), new ConsoleConsumer( "[SYSOUT]: " ) ).start();
        new StreamPumper( process.getErrorStream(), new ConsoleConsumer( "[SYSERR]: " ) ).start();

        getLogger().debug( "[LAUNCHER] Output pumpers ON" );

        try
        {
            getLogger().debug( "[LAUNCHER] Waiting for flashplayer termination" );
            int returnCode = process.waitFor();
            getLogger().debug( "[LAUNCHER] Flashplayer closed" );

            if ( returnCode == 0 )
            {
                getLogger().debug( "[LAUNCHER] Flashplayer exit as expected" );

                status = ThreadStatus.DONE;

                return;
            }
            else
            {
                getLogger().debug( "[LAUNCHER] Unexpected return code " + returnCode );

                status = ThreadStatus.ERROR;
                error = new Error( "Unexpected return code " + returnCode );
            }
        }
        catch ( InterruptedException e )
        {
            getLogger().debug( "[LAUNCHER] Process run error: " + e.getMessage() );

            status = ThreadStatus.ERROR;
            error = new Error( "Error while executing external command, process killed.", e );
        }
    }

    public void stop()
    {

        if ( process != null )
        {
            try
            {
                process.exitValue();
            }
            catch ( IllegalThreadStateException ex )
            {
                getLogger().debug( "[LAUNCHER] process has not been finished, destroying" );
                process.destroy();
            }
        }

        if ( useXvfb() )
        {
            try
            {
                getLogger().debug( "[LAUNCHER] process has not been finished, killing Xvfb" );
                Runtime.getRuntime().exec( new String[] { "killall", "Xvfb" } ).waitFor();
                Runtime.getRuntime().exec( new String[] { "killall", "xvfb-run" } ).waitFor();
                Runtime.getRuntime().exec( new String[] { "killall", new File( flashplayerCommand ).getName() } ).waitFor();
            }
            catch ( IOException e )
            {
                getLogger().error( "Error killing Xvfb", e );
            }
            catch ( InterruptedException e )
            {
                // ignore, process wake up call
            }

            if ( log != null )
            {
                try
                {
                    consoleLog.append( FileUtils.fileRead( log ) );
                }
                catch ( IOException e )
                {
                    getLogger().error( "Error reading Xvfb log", e );
                }

                log.delete();
            }

        }

    }

    public String getConsoleOutput()
    {
        return this.consoleLog.toString();
    }

}
