package org.sonatype.flexmojos.test;

import java.io.File;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.flexmojos.test.launcher.AsVmLauncher;
import org.sonatype.flexmojos.test.launcher.LaunchFlashPlayerException;
import org.sonatype.flexmojos.test.monitor.AsVmPing;
import org.sonatype.flexmojos.test.monitor.ResultHandler;

@Component( role = TestRunner.class, instantiationStrategy = "per-lookup" )
public class DefaultTestRunner
    extends AbstractLogEnabled
    implements TestRunner
{

    @Requirement( role = AsVmPing.class )
    private AsVmPing pinger;

    @Requirement( role = ResultHandler.class )
    private ResultHandler resultHandler;

    @Requirement( role = AsVmLauncher.class )
    private AsVmLauncher launcher;

    public List<String> run( TestRequest testRequest )
        throws TestRunnerException, LaunchFlashPlayerException
    {
        File swf = testRequest.getSwf();
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
            pinger.start( testRequest.getTestControlPort(), testRequest.getFirstConnectionTimeout(),
                          testRequest.getTestTimeout() );

            // Start a thread that receives the FlexUnit results.
            resultHandler.start( testRequest.getTestPort() );

            // Start the browser and run the FlexUnit tests.
            launcher.start( testRequest );

            // Wait until the tests are complete.
            while ( true )
            {
                getLogger().debug( "[MOJO] launcher " + launcher.getStatus() );
                getLogger().debug( "[MOJO] pinger " + pinger.getStatus() );
                getLogger().debug( "[MOJO] resultHandler " + resultHandler.getStatus() );

                if ( hasError( launcher, pinger, resultHandler ) )
                {
                    Throwable executionError = getError( launcher, pinger, resultHandler );
                    throw new TestRunnerException( executionError.getMessage() + " - " + swf, executionError );
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

}
