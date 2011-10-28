package org.sonatype.flexmojos.plugin.compiler.continuous;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.sonatype.flexmojos.plugin.compiler.MxmlcMojo;
import org.sonatype.flexmojos.test.TestRequest;
import org.sonatype.flexmojos.test.launcher.AsVmLauncher;
import org.sonatype.flexmojos.test.launcher.LaunchFlashPlayerException;

/**
 * @since 4.0
 * @goal cc
 * @requiresDependencyResolution compile
 * @phase compile
 * @threadSafe
 * @author Joa Ebert
 * @author Marvin Froeder
 * @requiresDirectInvocation
 */
public class MxmlcContinuousCompileMojo
    extends MxmlcMojo
{

    /**
     * The adl command
     * 
     * @parameter default-value="adl" expression="${flex.adl.command}"
     */
    private String adlCommand;

    /**
     * The flashplayer command
     * 
     * @parameter default-value="flashplayer" expression="${flex.flashPlayer.command}"
     */
    private String flashPlayerCommand;

    /**
     * Whether or not to spawn the Flash Player after each recompile.
     * 
     * @parameter expression="${flex.liveDevelopment}" default-value="true"
     */
    private boolean liveDevelopment;

    /**
     * @component
     */
    private AsVmLauncher vmLauncher;

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

        final File swf = new File( getOutput() );

        if ( !swf.exists() )
        {
            return;
        }

        vmLauncher.stop();

        TestRequest testRequest = new TestRequest();
        testRequest.setSwf( swf );
        testRequest.setAllowHeadlessMode( false );

        boolean isAirProject = getIsAirProject();
        testRequest.setUseAirDebugLauncher( isAirProject );
        if ( isAirProject )
        {
            testRequest.setAdlCommand( adlCommand );
            testRequest.setSwfDescriptor( createSwfDescriptor( swf ) );
        }
        else
        {
            testRequest.setFlashplayerCommand( flashPlayerCommand );
        }

        vmLauncher.start( testRequest );
    }

}
