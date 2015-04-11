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
package net.flexmojos.oss.plugin.compiler.continuous;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import net.flexmojos.oss.plugin.compiler.MxmlcMojo;
import net.flexmojos.oss.test.TestRequest;
import net.flexmojos.oss.test.launcher.AsVmLauncher;
import net.flexmojos.oss.test.launcher.LaunchFlashPlayerException;

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
    public void fmExecute()
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
