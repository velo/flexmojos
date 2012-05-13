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
package net.flexmojos.oss.plugin.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.velocity.texen.util.FileUtil;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import net.flexmojos.oss.coverage.CoverageReportException;
import net.flexmojos.oss.coverage.CoverageReportRequest;
import net.flexmojos.oss.coverage.CoverageReporter;
import net.flexmojos.oss.coverage.CoverageReporterManager;
import net.flexmojos.oss.plugin.AbstractMavenMojo;
import net.flexmojos.oss.plugin.SourcePathAware;
import net.flexmojos.oss.test.TestRequest;
import net.flexmojos.oss.test.TestRunner;
import net.flexmojos.oss.test.TestRunnerException;
import net.flexmojos.oss.test.launcher.LaunchFlashPlayerException;
import net.flexmojos.oss.test.report.TestCaseReport;
import net.flexmojos.oss.test.report.TestCoverageReport;
import net.flexmojos.oss.util.PathUtil;

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
 * @requiresDependencyResolution test
 * @phase test
 * @threadSafe
 */
public class TestRunMojo
    extends AbstractMavenMojo
    implements Mojo, SourcePathAware
{

    private static final String TEST_INFO = "Tests run: {0}, Failures: {1}, Errors: {2}, Time Elapsed: {3} sec";

    /**
     * The adl command
     * 
     * @parameter default-value="adl" expression="${flex.adl.command}"
     */
    private String adlCommand;

    /**
     * When true, allow flexmojos to launch xvfb-run to run test if it detects headless linux env
     * 
     * @parameter default-value="true" expression="${flex.allowHeadlessMode}"
     */
    private boolean allowHeadlessMode;

    /**
     * Uses instruments the bytecode (using apparat) to create test coverage report. Only the test-swf is affected by
     * this.
     * 
     * @parameter expression="${flex.coverage}"
     */
    protected boolean coverage;
    
    /**
     * Classes that shouldn't be include on code coverage analysis.
     * 
     * @parameter
     */
    private String[] coverageExclusions;

    /**
     * Location to save temporary files from coverage framework
     * 
     * @parameter default-value="${project.build.directory}/flexmojos"
     * @readonly
     */
    private File coverageDataDirectory;

    /**
     * Location to write coverage report
     * 
     * @parameter default-value="${project.build.directory}/coverage" expression="${flex.reportDestinationDir}"
     */
    protected File coverageOutputDirectory;

    /**
     * This is only used by flexmojos integration tests
     * 
     * @parameter expression="${flex.coverageOverwriteSourceRoots}"
     * @readonly
     */
    private String coverageOverwriteSourceRoots;

    /**
     * Framework that will be used to produce the coverage report. Accepts "emma" and "cobertura"
     * 
     * @parameter expression="${flex.coverageProvider}" default-value="cobertura"
     */
    private String coverageProvider;
    
    /**
     * Encoding used to generate coverage report
     * 
     * @parameter expression="${project.build.sourceEncoding}"
     */
    private String coverageReportEncoding;

    /**
     * @component
     */
    private CoverageReporterManager coverageReporterManager;

    /**
     * The coverage report format. Can be 'html', 'xml' and/or 'summaryXml'. Default value is 'html'.
     * 
     * @parameter
     */
    protected List<String> coverageReportFormat = Collections.singletonList( "html" );

    /**
     * The maven compile source roots. List of path elements that form the roots of ActionScript class
     * 
     * @parameter expression="${project.compileSourceRoots}"
     * @required
     * @readonly
     */
    private List<String> coverageSourceRoots;

    private Throwable executionError;

    private boolean failures = false;

    /**
     * Timeout for the first connection on ping Thread. That means how much time flexmojos will wait for Flashplayer be
     * loaded at first time.
     * 
     * @parameter default-value="20000" expression="${flex.firstConnectionTimeout}"
     */
    private int firstConnectionTimeout;

    /**
     * The flashplayer command
     * 
     * @parameter default-value="flashplayer" expression="${flex.flashPlayer.command}"
     */
    private String flashPlayerCommand;

    private int numErrors;

    private int numFailures;

    private int numTests;

    /**
     * Place where all test reports are saved
     */
    private File reportPath;

    /**
     * @parameter default-value="false" expression="${maven.test.skip}"
     */
    private boolean skip;

    /**
     * @parameter default-value="false" expression="${skipTests}"
     */
    private boolean skipTest;

    /**
     * If specified, the flexmojos will use this value as the control port to connect to during test runs.
     * 
     * @parameter expression="${flex.testControlPort}"
     */
    private Integer testControlPort;

    /**
     * @parameter default-value="false" expression="${maven.test.failure.ignore}"
     */
    private boolean testFailureIgnore;

    /**
     * @parameter expression="${project.build.testOutputDirectory}"
     * @readonly
     */
    private File testOutputDirectory;

    /**
     * If specified, flexmojos will use this value as the port to connect to during test runs.
     * 
     * @parameter expression="${flex.testPort}"
     */
    private Integer testPort;

    /**
     * @component role="net.flexmojos.oss.test.TestRunner"
     */
    private TestRunner testRunner;

    /**
     * Test timeout to wait for socket responding
     * 
     * @parameter default-value="2000" expression="${flex.testTimeout}"
     */
    private int testTimeout;

    private int time;

    /**
     * Create a server socket for receiving the test reports from FlexUnit. We read the test reports inside of a Thread.
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        // I'm not surefire, but ok
        reportPath = new File( project.getBuild().getDirectory(), "surefire-reports" );
        reportPath.mkdirs();

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

    public File[] getSourcePath()
    {
        List<File> files = new ArrayList<File>();

        if ( coverageOverwriteSourceRoots == null )
        {
            files.addAll( PathUtil.existingFilesList( coverageSourceRoots ) );
        }
        else
        {
            files.addAll( PathUtil.existingFilesList( Arrays.asList( coverageOverwriteSourceRoots.split( "," ) ) ) );
        }

        return files.toArray( new File[0] );
    }

    protected void run()
        throws MojoExecutionException, MojoFailureException
    {
        DirectoryScanner scan = new DirectoryScanner();
        scan.setIncludes( new String[] { "*.swf" } );
        scan.addDefaultExcludes();
        scan.setBasedir( testOutputDirectory );
        scan.scan();

        CoverageReporter reporter = null;
        if ( coverage )
        {
            try
            {
                reporter = coverageReporterManager.getReporter( coverageProvider );
                
                reporter.setExcludes( coverageExclusions );
            }
            catch ( CoverageReportException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
        }

        try
        {
            String[] swfs = scan.getIncludedFiles();
            runTests( swfs, reporter );
        }
        finally
        {
            if ( coverage )
            {
                CoverageReportRequest request =
                    new CoverageReportRequest( coverageDataDirectory, coverageReportFormat, coverageReportEncoding,
                                               coverageOutputDirectory, 
                                               new File( project.getBuild().getSourceDirectory() ) );
                try
                {
                    reporter.generateReport( request );
                    File index = new File(coverageOutputDirectory.getAbsolutePath() + "/index.html");
                    if(index.exists())
                    	FileUtils.copyFile(index, new File(coverageOutputDirectory.getAbsolutePath() + "/index.bak.html"));
                }
                catch ( CoverageReportException e )
                {
                    throw new MojoExecutionException( e.getMessage(), e );
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException( e.getMessage(), e );
                }
            }

        }
    }

    public void runTest( String swfName, Integer testPort, Integer testControlPort, CoverageReporter reporter )
        throws MojoExecutionException
    {
        File swf = new File( testOutputDirectory, swfName );

        getLog().debug( "Flexmojos test port: " + testPort + " - control: " + testControlPort );

        TestRequest testRequest = new TestRequest();
        testRequest.setTestControlPort( testControlPort );
        testRequest.setTestPort( testPort );
        testRequest.setSwf( swf );
        testRequest.setAllowHeadlessMode( allowHeadlessMode );
        testRequest.setTestTimeout( testTimeout );
        testRequest.setFirstConnectionTimeout( firstConnectionTimeout );

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

        if ( coverage )
        {
            reporter.instrument( swf, getSourcePath() );
        }

        try
        {
            List<String> results = runTest( testRequest );
            for ( String result : results )
            {
                TestCaseReport report = writeTestReport( result );
                if ( coverage )
                {
                    List<TestCoverageReport> coverageResult = report.getCoverage();
                    for ( TestCoverageReport testCoverageReport : coverageResult )
                    {
                        reporter.addResult( testCoverageReport.getClassname(), testCoverageReport.getTouchs() );
                    }
                }
            }
        }
        catch ( TestRunnerException e )
        {
            executionError = e;
        }
        catch ( LaunchFlashPlayerException e )
        {
            throw new MojoExecutionException(
                                              "Failed to launch Flash Player.  Probably java was not able to find flashplayer."
                                                  + "\n\t\tMake sure flashplayer is available on PATH"
                                                  + "\n\t\tor use -DflashPlayer.command=${flashplayer executable}"
                                                  + "\nRead more at: https://docs.sonatype.org/display/FLEXMOJOS/Running+unit+tests",
                                              e );
        }
    }

    public List<String> runTest( TestRequest testRequest )
        throws TestRunnerException, LaunchFlashPlayerException
    {
        List<String> results = testRunner.run( testRequest );
        return results;
    }

    public void runTests( String[] swfs, CoverageReporter reporter )
        throws MojoExecutionException, MojoFailureException
    {
        if ( testPort == null )
        {
            // This will fail if you are trying to run the "test-run" goal in a separate maven execution from the
            // "test-compile" goal!
            testPort = getFromPluginContext( TestCompilerMojo.FLEXMOJOS_TEST_PORT );
        }
        if ( testControlPort == null )
        {
            // This will fail if you are trying to run the "test-run" goal in a separate maven execution from the
            // "test-compile" goal!
            testControlPort = getFromPluginContext( TestCompilerMojo.FLEXMOJOS_TEST_CONTROL_PORT );
        }
        getLog().debug( "Found " + swfs.length + " test runners:\n" + Arrays.toString( swfs ) );
        getLog().debug( "Using test port '" + testPort + "' and test control port '" + testControlPort + "'" );
        for ( String swfName : swfs )
        {
            runTest( swfName, testPort, testControlPort, reporter );
        }
    }

    protected void tearDown()
        throws MojoExecutionException, MojoFailureException
    {

        getLog().info( "------------------------------------------------------------------------" );
        getLog().info( MessageFormat.format( TEST_INFO, new Object[] { new Integer( numTests ),
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

    /**
     * Write a test report to disk.
     * 
     * @param reportString the report to write.
     * @return
     * @throws MojoExecutionException
     */
    private TestCaseReport writeTestReport( final String reportString )
        throws MojoExecutionException
    {
        // Parse the report.
        TestCaseReport report;
        try
        {
            report = new TestCaseReport( Xpp3DomBuilder.build( new StringReader( reportString ) ) );
        }
        catch ( XmlPullParserException e )
        {
            // should never happen
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( IOException e )
        {
            // should never happen
            throw new MojoExecutionException( e.getMessage(), e );
        }

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

        return report;
    }

}
