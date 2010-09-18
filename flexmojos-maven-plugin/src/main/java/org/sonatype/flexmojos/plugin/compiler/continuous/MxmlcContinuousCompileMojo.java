package org.sonatype.flexmojos.plugin.compiler.continuous;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.StreamPumper;
import org.sonatype.flexmojos.plugin.compiler.MxmlcMojo;
import org.sonatype.flexmojos.test.launcher.FlashPlayerShutdownHook;
import org.sonatype.flexmojos.test.launcher.LaunchFlashPlayerException;
import org.sonatype.flexmojos.util.OSUtils;

/**
 * @since 4.0
 * @goal cc
 * @requiresDependencyResolution compile
 * @phase compile
 * @configurator flexmojos
 * @threadSafe
 * @author Joa Ebert
 * @requiresDirectInvocation
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
