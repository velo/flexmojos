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
package org.sonatype.flexmojos.plugin.compiler;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.StreamPumper;
import org.sonatype.flexmojos.test.launcher.FlashPlayerShutdownHook;
import org.sonatype.flexmojos.test.launcher.LaunchFlashPlayerException;
import org.sonatype.flexmojos.util.OSUtils;

import java.io.File;
import java.io.IOException;

/**
 * @since 4.0
 * @goal cc
 * @requiresDependencyResolution compile
 * @phase compile
 * @configurator flexmojos
 * @threadSafe
 * @author Joa Ebert
 */
public class MxmlcContinuousCompileMojo
    extends MxmlcMojo
{
    /**
     * Whether or not to spawn the Flash Player after each recompile.
     * 
     * @parameter expression="${flex.liveDevelopment}" default-value="true"
     */
    private boolean liveDevelopment;

    /**
     * Can be of type <code>&lt;argument&gt;</code>
     * 
     * @parameter expression="${flex.flashPlayer.command}"
     */
    private String flashPlayerCommand;

    private Process process;

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        //
        // We have ot set quick to true since isCompilationRequired does a check
        // only if quick has been set to true.
        //

        super.quick = true;

        //
        // Use default if Flash Player command has not been set.
        //

        if ( null == flashPlayerCommand )
        {
            flashPlayerCommand = OSUtils.getPlatformDefaultFlashPlayer();
        }

        try
        {
            showInfo();

            while ( !Thread.interrupted() )
            {
                if ( isCompilationRequired() )
                {
                    //
                    // We have to compile so let's hand the job to
                    // the MxmlcMojo implementation and do the actual work.
                    //

                    super.execute();

                    showInfo();
                    try
                    {
                        spawnFlashplayer();
                    }
                    catch ( final LaunchFlashPlayerException launchFlashPlayerException )
                    {
                        getLog().warn( launchFlashPlayerException );
                    }

                    Thread.sleep( 4000L );
                }
                else
                {
                    Thread.sleep( 2000L );
                }
            }
        }
        catch ( final InterruptedException interruptException )
        {
            // nothing to do here
        }
    }

    protected void showInfo()
    {
        getLog().info( "Waiting for files to compile ..." );
    }

    protected void spawnFlashplayer()
        throws LaunchFlashPlayerException
    {
        if ( !liveDevelopment )
        {
            return;
        }

        final String output = getOutput();
        final File outputFile = new File( output );

        if ( !outputFile.exists() )
        {
            return;
        }

        if ( null != process )
        {
            process.destroy();
            process = null;
        }

        runFlashplayer( flashPlayerCommand, output );
        Runtime.getRuntime().addShutdownHook( new FlashPlayerShutdownHook( process ) );
    }

    // TODO taken from AsVmLauncher
    private void runFlashplayer( final String asvmCommand, final String targetFile )
        throws LaunchFlashPlayerException
    {
        try
        {
            process = Runtime.getRuntime().exec( new String[] { asvmCommand, targetFile } );
            new StreamPumper( process.getInputStream(), new ConsoleConsumer( "[SYSOUT]: " ) ).start();
            new StreamPumper( process.getErrorStream(), new ConsoleConsumer( "[SYSERR]: " ) ).start();
        }
        catch ( IOException e )
        {
            throw new LaunchFlashPlayerException( "Failed to launch Flash Player.", e );
        }
    }

    // TODO taken from AsVmLauncher
    private class ConsoleConsumer
        implements StreamConsumer
    {
        private final String prefix;

        public ConsoleConsumer( final String prefix )
        {
            this.prefix = prefix;
        }

        public void consumeLine( final String line )
        {
            if ( "\n".equals( line ) )
            {
                return;
            }

            getLog().debug( prefix + line );
        }
    }
}
