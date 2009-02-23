/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.MessageFormat;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.flexmojos.AbstractIrvinMojo;
import org.sonatype.flexmojos.test.report.TestCaseReport;
import org.sonatype.flexmojos.test.util.XStreamFactory;
import org.sonatype.flexmojos.utilities.MavenUtils;

import com.thoughtworks.xstream.XStream;

/**
 * Goal to run unit tests on Flex.  It does support the following frameworks:
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

    private static final String END_OF_TEST_RUN = "<endOfTestRun/>";

    private static final String END_OF_TEST_SUITE = "</testsuite>";

    private static final String END_OF_TEST_ACK = "<endOfTestRunAck/>";

    private static final char NULL_BYTE = '\u0000';

    private static final String POLICY_FILE_REQUEST = "<policy-file-request/>";

    final static String DOMAIN_POLICY =
        "<cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"{0}\" /></cross-domain-policy>";

    private boolean failures = false;

    private boolean complete;

    /**
     * Socket connect port for flex/java communication
     * 
     * @parameter default-value="13539"
     */
    private int testPort;

    /**
     * Socket timeout for flex/java communication in milliseconds
     * 
     * @parameter default-value="60000"
     */
    private int socketTimeout = 60000; // milliseconds

    /**
     * Can be of type <code>&lt;argument&gt;</code>
     * 
     * @parameter
     */
    private List<String> flexUnitCommand;

    protected File swf;

    private MojoExecutionException executionError; // BAD IDEA

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
            // TODO need to check problems on MAC OS
            // } else if (GraphicsEnvironment.isHeadless()) {
            // getLog().error("Can't run flexunit in headless enviroment.");
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
    private void receiveFlexUnitResults()
        throws MojoExecutionException
    {
        // Start a thread to accept a client connection.
        final Thread thread = new Thread()
        {
            private ServerSocket serverSocket = null;

            private Socket clientSocket = null;

            private InputStream in = null;

            private OutputStream out = null;

            public void run()
            {
                try
                {
                    openServerSocket();
                    openClientSocket();

                    StringBuffer buffer = new StringBuffer();
                    int bite = -1;

                    while ( ( bite = in.read() ) != -1 )
                    {
                        final char chr = (char) bite;

                        if ( chr == NULL_BYTE )
                        {
                            final String data = buffer.toString();
                            // getLog().debug( "Recivied data: " + data );
                            buffer = new StringBuffer();

                            if ( data.equals( POLICY_FILE_REQUEST ) )
                            {
                                getLog().debug( "Send policy file" );

                                sendPolicyFile();
                                closeClientSocket();
                                openClientSocket();
                            }
                            else if ( data.endsWith( END_OF_TEST_SUITE ) )
                            {
                                getLog().debug( "End test suite" );

                                saveTestReport( data );
                            }
                            else if ( data.equals( END_OF_TEST_RUN ) )
                            {
                                getLog().debug( "End test run" );
                                sendAcknowledgement();
                            }
                        }
                        else
                        {
                            buffer.append( chr );
                        }
                    }

                    getLog().debug( "Socket buffer " + buffer );
                }
                catch ( MojoExecutionException be )
                {
                    executionError = be;

                    try
                    {
                        sendAcknowledgement();
                    }
                    catch ( IOException e )
                    {
                        // ignore
                    }
                }
                catch ( SocketTimeoutException e )
                {
                    executionError = new MojoExecutionException( "timeout waiting for flexunit report", e );
                }
                catch ( IOException e )
                {
                    executionError = new MojoExecutionException( "error receiving report from flexunit", e );
                }
                finally
                {
                    // always stop the server loop
                    complete = true;

                    closeClientSocket();
                    closeServerSocket();
                }
            }

            private void sendPolicyFile()
                throws IOException
            {
                out.write( MessageFormat.format( DOMAIN_POLICY, new Object[] { Integer.toString( testPort ) } ).getBytes() );

                out.write( NULL_BYTE );

                getLog().debug( "sent policy file" );
            }

            private void saveTestReport( final String report )
                throws MojoExecutionException
            {
                writeTestReport( report );

                getLog().debug( "end of test" );
            }

            private void sendAcknowledgement()
                throws IOException
            {
                out.write( END_OF_TEST_ACK.getBytes() );
                out.write( NULL_BYTE );

                getLog().debug( "end of test run" );
            }

            private void openServerSocket()
                throws IOException
            {
                serverSocket = new ServerSocket( testPort );
                serverSocket.setSoTimeout( socketTimeout );

                getLog().debug( "opened server socket" );
            }

            private void closeServerSocket()
            {
                if ( serverSocket != null )
                {
                    try
                    {
                        serverSocket.close();
                    }
                    catch ( IOException e )
                    {
                        // ignore
                    }
                }
            }

            private void openClientSocket()
                throws IOException
            {
                // This method blocks until a connection is made.
                clientSocket = serverSocket.accept();

                getLog().debug( "accepting data from client" );

                in = clientSocket.getInputStream();
                out = clientSocket.getOutputStream();
            }

            private void closeClientSocket()
            {
                // Close the output stream.
                if ( out != null )
                {
                    try
                    {
                        out.close();
                    }
                    catch ( IOException e )
                    {
                        // ignore
                    }
                }

                // Close the input stream.
                if ( in != null )
                {
                    try
                    {
                        in.close();
                    }
                    catch ( IOException e )
                    {
                        // ignore
                    }
                }

                // Close the client socket.
                if ( clientSocket != null )
                {
                    try
                    {
                        clientSocket.close();
                    }
                    catch ( IOException e )
                    {
                        // ignore
                    }
                }
            }
        };

        thread.start();
    }

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
            xs.toXML( report, writer );
            writer.flush();
        }
        catch ( Exception e )
        {
            IOUtil.close( writer );
            throw new MojoExecutionException( "error writing report to disk", e );
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

    // /**
    // * crafts a simple junit type log message.
    // */
    // private String formatLogReport( final Element root )
    // {
    // int numFailures = Integer.parseInt( root.valueOf( "@failures" ) );
    // int numErrors = Integer.parseInt( root.valueOf( "@errors" ) );
    // int numTests = Integer.parseInt( root.valueOf( "@tests" ) );
    // int time = Integer.parseInt( root.valueOf( "@time" ) );
    //
    // final StringBuffer msg = new StringBuffer();
    // msg.append( "Tests run: " );
    // msg.append( numTests );
    // msg.append( ", Failures: " );
    // msg.append( numFailures );
    // msg.append( ", Errors: " );
    // msg.append( numErrors );
    // msg.append( ", Time Elapsed: " );
    // msg.append( time );
    // msg.append( " sec" );
    //
    // return msg.toString();
    // }

    @Override
    protected void run()
        throws MojoExecutionException, MojoFailureException
    {
        // Start a thread that receives the FlexUnit results.
        receiveFlexUnitResults();

        getLog().info( "flexunit setup args: " + flexUnitCommand );

        // Start the browser and run the FlexUnit tests.
        final FlexUnitLauncher browser = new FlexUnitLauncher( flexUnitCommand, getLog() );
        try
        {
            browser.runTests( swf );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Error launching the test runner.", e );
        }

        // Wait until the tests are complete.
        while ( !complete )
        {
            try
            {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
        }

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
