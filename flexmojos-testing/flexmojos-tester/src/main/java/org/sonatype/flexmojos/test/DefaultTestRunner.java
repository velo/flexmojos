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
package org.sonatype.flexmojos.test;

import java.io.File;
import java.util.List;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.sonatype.flexmojos.test.launcher.AsVmLauncher;
import org.sonatype.flexmojos.test.launcher.LaunchFlashPlayerException;
import org.sonatype.flexmojos.test.monitor.AsVmPing;
import org.sonatype.flexmojos.test.monitor.ResultHandler;

@Component( role = TestRunner.class )
public class DefaultTestRunner
    extends AbstractLogEnabled
    implements TestRunner, Contextualizable
{

    @Requirement( role = AsVmPing.class )
    private AsVmPing pinger;

    @Requirement( role = ResultHandler.class )
    private ResultHandler resultHandler;

    @Requirement( role = AsVmLauncher.class )
    private AsVmLauncher launcher;

    @Configuration( value = "13540" )
    private int testControlPort;

    @Configuration( value = "13539" )
    private int testReportPort;

    @Configuration( value = "20000" )
    private int firstConnectionTimeout;

    @Configuration( value = "5000" )
    private int testTimeout;

    private PlexusContainer container;

    public List<String> run( File swf )
        throws TestRunnerException, LaunchFlashPlayerException
    {
        init();

        if ( swf == null )
        {
            throw new TestRunnerException( "Target SWF not defined" );
        }

        if ( !swf.isFile() )
        {
            throw new TestRunnerException( "Target SWF not found " + swf );
        }

        getLogger().info( "Running tests " + swf );

        try
        {
            // Start a thread that pings flashplayer to be sure if it still alive.
            pinger.start( testControlPort, firstConnectionTimeout, testTimeout );

            // Start a thread that receives the FlexUnit results.
            resultHandler.start( testReportPort );

            // Start the browser and run the FlexUnit tests.
            launcher.start( swf );

            // Wait until the tests are complete.
            while ( true )
            {
                getLogger().debug( "[MOJO] launcher " + launcher.getStatus() );
                getLogger().debug( "[MOJO] pinger " + pinger.getStatus() );
                getLogger().debug( "[MOJO] resultHandler " + resultHandler.getStatus() );

                if ( hasError( launcher, pinger, resultHandler ) )
                {
                    Throwable executionError = getError( launcher, pinger, resultHandler );
                    throw new TestRunnerException( executionError.getMessage() + swf, executionError );
                }

                if ( hasDone( launcher ) )
                {
                    for ( int i = 0; i < 3; i++ )
                    {
                        if ( hasDone( resultHandler ) && hasDone( pinger ) )
                        {
                            List<String> results = resultHandler.getTestReportData();
                            return results; // expected exit!
                        }
                        sleep( 500 );
                    }

                    // the flashplayer is closed, but the sockets still running...
                    throw new TestRunnerException(
                                                   "Invalid state: the flashplayer is closed, but the sockets still running..." );
                }

                sleep( 1000 );
            }
        }
        finally
        {
            stop( launcher, pinger, resultHandler );
            launcher = null;
            pinger = null;
            resultHandler = null;
        }
    }

    private void init()
        throws TestRunnerException
    {
        try
        {
            if ( launcher == null )
            {
                launcher = (AsVmLauncher) container.lookup( AsVmLauncher.class.getName() );
            }

            if ( pinger == null )
            {
                pinger = (AsVmPing) container.lookup( AsVmPing.class.getName() );
            }

            if ( resultHandler == null )
            {
                resultHandler = (ResultHandler) container.lookup( ResultHandler.class.getName() );
            }
        }
        catch ( ComponentLookupException e )
        {
            throw new TestRunnerException( "Error looking up for components", e );
        }
    }

    private void sleep( int time )
    {
        try
        {
            Thread.sleep( time );
        }
        catch ( InterruptedException e )
        {
            // no worries
        }
    }

    private void stop( ControlledThread... threads )
    {
        for ( ControlledThread controlledThread : threads )
        {
            // only stop if is running
            if ( controlledThread != null && ThreadStatus.RUNNING.equals( controlledThread.getStatus() ) )
            {
                try
                {
                    controlledThread.stop();
                }
                catch ( Throwable e )
                {
                    getLogger().debug( "[MOJO] Error stopping " + controlledThread.getClass(), e );
                }

                try
                {
                    container.release( controlledThread );
                }
                catch ( ComponentLifecycleException e )
                {
                    getLogger().debug( "[MOJO] Error releasing " + controlledThread.getClass(), e );
                }
            }
        }
    }

    private boolean hasDone( ControlledThread... threads )
    {
        for ( ControlledThread controlledThread : threads )
        {
            if ( ThreadStatus.DONE.equals( controlledThread.getStatus() ) )
            {
                return true;
            }
        }
        return false;
    }

    private Throwable getError( ControlledThread... threads )
    {
        for ( ControlledThread controlledThread : threads )
        {
            if ( controlledThread.getError() != null )
            {
                return controlledThread.getError();
            }
        }

        throw new IllegalStateException( "No error found!" );
    }

    private boolean hasError( ControlledThread... threads )
    {
        for ( ControlledThread controlledThread : threads )
        {
            if ( ThreadStatus.ERROR.equals( controlledThread.getStatus() ) )
            {
                return true;
            }
        }
        return false;
    }

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

}
