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

import static org.sonatype.flexmojos.test.threads.ControlledThreadUtil.getError;
import static org.sonatype.flexmojos.test.threads.ControlledThreadUtil.hasDone;
import static org.sonatype.flexmojos.test.threads.ControlledThreadUtil.hasError;
import static org.sonatype.flexmojos.test.threads.ControlledThreadUtil.stop;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.MessageFormat;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
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

    protected File swf;

    private Error executionError;

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
     * @component
     */
    private AsVmLauncher asVmLauncher;

    /**
     * @component
     */
    private ResultHandler resultHandler;

    /**
     * @component
     */
    private AsVmControl asVmControl;

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
        else if ( swf == null || !swf.exists() )
        {
            getLog().warn( "Skipping test run. Runner not found: " + swf );
        }
        else
        {
            run();
            tearDown();
        }
    }

    /**
     * Called by Ant to execute the task.
     */
    @Override
    protected void setUp()
        throws MojoExecutionException, MojoFailureException
    {
        swf = new File( build.getTestOutputDirectory(), "TestRunner.swf" );
        reportPath = new File( build.getDirectory(), "surefire-reports" );
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

        getLog().debug( "Running " + name );
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
        getLog().info( "Starting tests" );

        try
        {
            // Start a thread that pings flashplayer to be sure if it still alive.
            asVmControl.init( testControlPort, firstConnectionTimeout, testTimeout );
            run( asVmControl );

            // Start a thread that receives the FlexUnit results.
            resultHandler.init( testPort );
            run( resultHandler );

            // Start the browser and run the FlexUnit tests.
            asVmLauncher.init( flashPlayerCommand, swf, allowHeadlessMode );
            run( asVmLauncher );

            // Wait until the tests are complete.
            while ( true )
            {
                getLog().debug( "asVmLauncher " + asVmLauncher.getStatus() );
                getLog().debug( "asVmControl " + asVmControl.getStatus() );
                getLog().debug( "resultHandler " + resultHandler.getStatus() );

                if ( hasError( asVmLauncher, asVmControl, resultHandler ) )
                {
                    this.executionError = getError( asVmLauncher, asVmControl, resultHandler );
                    getLog().error( executionError.getMessage(), executionError );
                    return;
                }

                if ( hasDone( resultHandler ) )
                {
                    List<String> results = resultHandler.getTestReportData();
                    for ( String result : results )
                    {
                        writeTestReport( result );
                    }
                    return;
                }

                if ( hasDone( asVmLauncher ) )
                {
                    this.executionError = new Error( "Flash Player was closed!" );
                    return;
                }

                Thread.sleep( 1000 );
            }
        }
        catch ( InterruptedException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        finally
        {
            stop( asVmLauncher, asVmControl, resultHandler );
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
                getLog().debug( "Error running: " + thread.getClass(), e );
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
                throw executionError;
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
}
