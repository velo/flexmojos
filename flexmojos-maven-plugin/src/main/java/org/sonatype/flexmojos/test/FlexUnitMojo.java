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
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.MessageFormat;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.flexmojos.AbstractIrvinMojo;
import org.sonatype.flexmojos.test.report.TestCaseReport;
import org.sonatype.flexmojos.test.threads.AsVmControl;
import org.sonatype.flexmojos.test.threads.AsVmLauncher;
import org.sonatype.flexmojos.test.threads.ControlledThread;
import org.sonatype.flexmojos.test.threads.ResultHandler;
import org.sonatype.flexmojos.test.threads.ThreadStatus;
import org.sonatype.flexmojos.test.util.XStreamFactory;
import org.sonatype.flexmojos.utilities.MavenUtils;

import com.thoughtworks.xstream.XStream;

/**
 * Goal to run unit tests on Flex. It does support the following frameworks:
 * <ul>
 * <li>Adobe Flexunit</li>
 * <li>FUnit</li>
 * <li>asunit</li>
 * <li>advanced flex debug</li>
 * <li>FlexMonkey</li>
 * </ul>
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 1.0
 * @goal test-run
 * @requiresDependencyResolution
 * @phase test
 */
public class FlexUnitMojo
    extends AbstractIrvinMojo
    implements Contextualizable
{

    private static final String TEST_INFO = "Tests run: {0}, Failures: {1}, Errors: {2}, Time Elpased: {3} sec";

    private boolean failures = false;

    /**
     * Socket connect port for flex/java communication to transfer tests results
     * 
     * @parameter default-value="13539" expression="${testPort}"
     */
    private int testPort;

    /**
     * Socket connect port for flex/java communication to control if flashplayer is alive
     * 
     * @parameter default-value="13540" expression="${testControlPort}"
     */
    private int testControlPort;

    /**
     * Can be of type <code>&lt;argument&gt;</code>
     * 
     * @parameter expression="${flashPlayer.command}"
     */
    private String flashPlayerCommand;

    /**
     * @parameter expression="${project.build.testOutputDirectory}"
     * @readonly
     */
    private File testOutputDirectory;

    private Throwable executionError;

    /**
     * @parameter default-value="false" expression="${maven.test.skip}"
     */
    private boolean skip;

    /**
     * @parameter default-value="false" expression="${skipTests}"
     */
    private boolean skipTest;

    /**
     * Place where all test reports are saved
     */
    private File reportPath;

    /**
     * @parameter default-value="false" expression="${maven.test.failure.ignore}"
     */
    private boolean testFailureIgnore;

    private int numTests;

    private int numFailures;

    private int numErrors;

    private int time;

    /**
     * When true, allow flexmojos to launch xvfb-run to run test if it detects headless linux env
     * 
     * @parameter default-value="true" expression="${allowHeadlessMode}"
     */
    private boolean allowHeadlessMode;

    /**
     * Timeout for the first connection on ping Thread. That means how much time flexmojos will wait for Flashplayer be
     * loaded at first time.
     * 
     * @parameter default-value="20000" expression="${firstConnectionTimeout}"
     */
    private int firstConnectionTimeout;

    /**
     * Test timeout to wait for socket responding
     * 
     * @parameter default-value="2000" expression="${testTimeout}"
     */
    private int testTimeout;

    private PlexusContainer plexus;

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().info(
                       "flexmojos " + MavenUtils.getFlexMojosVersion()
                           + " - GNU GPL License (NO WARRANTY) - See COPYRIGHT file" );

        setUp();

        if ( skip || skipTest )
        {
            getLog().info( "Skipping test phase." );
        }
        else if ( testOutputDirectory == null || !testOutputDirectory.isDirectory() )
        {
            getLog().warn( "Skipping test run. Runner not found: " + testOutputDirectory );
        }
        else
        {
            run();
            tearDown();
        }
    }

    @Override
    protected void setUp()
        throws MojoExecutionException, MojoFailureException
    {
        reportPath = new File( build.getDirectory(), "surefire-reports" ); // I'm not surefire, but ok
        reportPath.mkdirs();
    }

    /**
     * Create a server socket for receiving the test reports from FlexUnit. We read the test reports inside of a Thread.
     */

    /**
     * Write a test report to disk.
     * 
     * @param reportString the report to write.
     * @throws MojoExecutionException
     */
    private void writeTestReport( final String reportString )
        throws MojoExecutionException
    {

        XStream xs = XStreamFactory.getXStreamInstance();

        // Parse the report.
        TestCaseReport report = (TestCaseReport) xs.fromXML( reportString );

        // Get the test attributes.
        final String name = report.getName();
        final int numFailures = report.getFailures();
        final int numErrors = report.getErrors();
        final int totalProblems = numFailures + numErrors;

        getLog().debug( "[MOJO] Test report of " + name );
        getLog().debug( reportString );

        // Get the output file name.
        final File file = new File( reportPath, "TEST-" + name.replace( "::", "." ) + ".xml" );

        FileWriter writer = null;
        try
        {
            writer = new FileWriter( file );
            IOUtil.copy( reportString, writer );
            writer.flush();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to save test result report", e );
        }
        finally
        {
            IOUtil.close( writer );
        }

        // Pretty print the document to disk.
        // final XMLWriter writer = new XMLWriter( new FileOutputStream( file ), format );
        // writer.write( document );
        // writer.close();

        // First write the report, then fail the build if the test failed.
        if ( totalProblems > 0 )
        {
            failures = true;

            getLog().warn( "Unit test " + name + " failed." );

        }

        this.numTests += report.getTests();
        this.numErrors += report.getErrors();
        this.numFailures += report.getFailures();

    }

    @Override
    protected void run()
        throws MojoExecutionException, MojoFailureException
    {
        DirectoryScanner scan = new DirectoryScanner();
        scan.setIncludes( new String[] { "*.swf" } );
        scan.addDefaultExcludes();
        scan.setBasedir( testOutputDirectory );
        scan.scan();

        String[] swfs = scan.getIncludedFiles();
        for ( String swfName : swfs )
        {
            run( new File( testOutputDirectory, swfName ) );
        }
    }

    protected void run( File swf )
        throws MojoExecutionException
    {
        getLog().info( "Running tests " + swf );

        AsVmControl asVmControl = null;
        ResultHandler resultHandler = null;
        AsVmLauncher asVmLauncher = null;
        try
        {
            // Start a thread that pings flashplayer to be sure if it still alive.
            asVmControl = lookup( AsVmControl.class );
            asVmControl.init( testControlPort, firstConnectionTimeout, testTimeout );
            run( asVmControl );

            // Start a thread that receives the FlexUnit results.
            resultHandler = lookup( ResultHandler.class );
            resultHandler.init( testPort );
            run( resultHandler );

            // Start the browser and run the FlexUnit tests.
            asVmLauncher = lookup( AsVmLauncher.class );
            asVmLauncher.init( flashPlayerCommand, swf, allowHeadlessMode );
            run( asVmLauncher );

            // Wait until the tests are complete.
            while ( true )
            {
                getLog().debug( "[MOJO] asVmLauncher " + asVmLauncher.getStatus() );
                getLog().debug( "[MOJO] asVmControl " + asVmControl.getStatus() );
                getLog().debug( "[MOJO] resultHandler " + resultHandler.getStatus() );

                if ( hasError( asVmLauncher, asVmControl, resultHandler ) )
                {
                    this.executionError = getError( asVmLauncher, asVmControl, resultHandler );
                    getLog().error( executionError.getMessage() + swf, executionError );
                    numTests++;
                    numErrors++;
                    return;
                }

                if ( hasDone( asVmLauncher ) )
                {
                    for ( int i = 0; i < 3; i++ )
                    {
                        if ( hasDone( resultHandler ) && hasDone( asVmControl ) )
                        {
                            List<String> results = resultHandler.getTestReportData();
                            for ( String result : results )
                            {
                                writeTestReport( result );
                            }
                            return; // expected exit!
                        }
                        sleep( 500 );
                    }

                    // the flashplayer is closed, but the sockets still running...

                    this.executionError =
                        new IllegalStateException( "the flashplayer is closed, but the sockets still running" );
                    getLog().error( "Invalid state: the flashplayer is closed, but the sockets still running..." );
                    numTests++;
                    numErrors++;
                    return; // abnormal exit!
                }

                sleep( 1000 );
            }
        }
        finally
        {
            stop( asVmLauncher, asVmControl, resultHandler );
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

    @SuppressWarnings( "unchecked" )
    private <E> E lookup( Class<E> clazz )
        throws MojoExecutionException
    {
        String role = clazz.getName();
        getLog().debug( "[MOJO] Looking up for " + role );
        try
        {
            return (E) plexus.lookup( role );
        }
        catch ( ComponentLookupException e )
        {
            throw new MojoExecutionException( "Unable to lookup for " + role, e );
        }
    }

    private void run( final ControlledThread thread )
    {
        Thread t = new Thread( thread );
        t.setUncaughtExceptionHandler( new UncaughtExceptionHandler()
        {
            public void uncaughtException( Thread t, Throwable e )
            {
                if ( thread.getStatus() != ThreadStatus.ERROR )
                {
                    thread.setStatus( ThreadStatus.ERROR );
                    thread.setError( new Error( "Runtime error running: " + thread.getClass(), e ) );
                }
                getLog().debug( "[MOJO] Error running: " + thread.getClass(), e );
            }
        } );
        t.setDaemon( true );
        t.start();
        Thread.yield();
    }

    @Override
    protected void tearDown()
        throws MojoExecutionException, MojoFailureException
    {

        getLog().info( "------------------------------------------------------------------------" );
        getLog().info(
                       MessageFormat.format( TEST_INFO, new Object[] { new Integer( numTests ),
                           new Integer( numErrors ), new Integer( numFailures ), new Integer( time ) } ) );

        if ( !testFailureIgnore )
        {
            if ( executionError != null )
            {
                throw new MojoExecutionException( executionError.getMessage(), executionError );
            }

            if ( failures )
            {
                throw new MojoExecutionException( "Some tests fail" );
            }
        }
        else
        {
            if ( executionError != null )
            {
                getLog().error( executionError.getMessage(), executionError );
            }

            if ( failures )
            {
                getLog().error( "Some tests fail" );
            }
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
                    getLog().debug( "[MOJO] Error stopping " + controlledThread.getClass(), e );
                }
                finally
                {
                    try
                    {
                        plexus.release( controlledThread );
                    }
                    catch ( ComponentLifecycleException e )
                    {
                        getLog().debug( "[MOJO] Error releasing " + controlledThread.getClass(), e );
                        // just releasing ignoring
                    }
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

    private Error getError( ControlledThread... threads )
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
        plexus = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

}
