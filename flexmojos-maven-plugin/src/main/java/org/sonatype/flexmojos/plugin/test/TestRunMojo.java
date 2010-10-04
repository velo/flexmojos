package org.sonatype.flexmojos.plugin.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.flexmojos.coverage.CoverageReportException;
import org.sonatype.flexmojos.coverage.CoverageReportRequest;
import org.sonatype.flexmojos.coverage.CoverageReporter;
import org.sonatype.flexmojos.coverage.CoverageReporterManager;
import org.sonatype.flexmojos.plugin.AbstractMavenMojo;
import org.sonatype.flexmojos.plugin.SourcePathAware;
import org.sonatype.flexmojos.test.TestRequest;
import org.sonatype.flexmojos.test.TestRunner;
import org.sonatype.flexmojos.test.TestRunnerException;
import org.sonatype.flexmojos.test.launcher.LaunchFlashPlayerException;
import org.sonatype.flexmojos.test.report.TestCaseReport;
import org.sonatype.flexmojos.test.report.TestCoverageReport;
import org.sonatype.flexmojos.util.PathUtil;

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
     * Can be of type <code>&lt;argument&gt;</code>
     * 
     * @parameter expression="${flex.adl.command}"
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
     * Location to save temporary files from coverage framework
     * 
     * @parameter default-value="${project.build.directory}/flexmojos"
     * @readonly
     */
    private File coverageDataDirectory;

    /**
     * Framework that will be used to produce the coverage report. Accepts "emma" and "cobertura"
     * 
     * @parameter expression="${flex.coverageProvider}" default-value="cobertura"
     */
    private String coverageProvider;

    /**
     * Location to write coverage report
     * 
     * @parameter default-value="${project.build.directory}/coverage" expression="${flex.reportDestinationDir}"
     */
    protected File coverageOutputDirectory;

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

    /**
     * This is only used by flexmojos integration tests
     * 
     * @parameter expression="${flex.coverageOverwriteSourceRoots}"
     * @readonly
     */
    private String coverageOverwriteSourceRoots;

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
     * Can be of type <code>&lt;argument&gt;</code>
     * 
     * @parameter expression="${flex.flashPlayer.command}"
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
     * Socket connect port for flex/java communication to control if flashplayer is alive
     * 
     * @parameter default-value="13540" expression="${flex.testControlPort}"
     */
    private int testControlPort;

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
     * @component role="org.sonatype.flexmojos.test.TestRunner"
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

    private File getSwfDescriptor( File swf )
        throws MojoExecutionException
    {
        Reader reader = null;
        FileWriter writer = null;
        try
        {
            reader =
                new InputStreamReader( getClass().getResourceAsStream( "/templates/test/air-descriptor-template.xml" ) );

            Map<String, String> variables = new LinkedHashMap<String, String>();
            variables.put( "swf", swf.getName() );

            InterpolationFilterReader filterReader = new InterpolationFilterReader( reader, variables );

            File destFile = new File( swf.getParentFile(), FilenameUtils.getBaseName( swf.getName() ) + ".xml" );
            writer = new FileWriter( destFile );

            IOUtil.copy( filterReader, writer );

            return destFile;
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Fail to create test air descriptor", e );
        }
        finally
        {
            IOUtil.close( reader );
            IOUtil.close( writer );
        }
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
            }
            catch ( CoverageReportException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
        }

        try
        {
            String[] swfs = scan.getIncludedFiles();
            getLog().debug( "Found " + swfs.length + " test runners:\n" + Arrays.toString( swfs ) );
            for ( String swfName : swfs )
            {
                try
                {
                    File swf = new File( testOutputDirectory, swfName );
                    Integer testPort = getFromPluginContext( TestCompilerMojo.FLEXMOJOS_TEST_PORT );

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
                        testRequest.setSwfDescriptor( getSwfDescriptor( swf ) );
                    }
                    else
                    {
                        testRequest.setFlashplayerCommand( flashPlayerCommand );
                    }

                    if ( coverage )
                    {
                        reporter.instrument( swf, getSourcePath() );
                    }

                    List<String> results = testRunner.run( testRequest );
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
                }
                catch ( CoverageReportException e )
                {
                    throw new MojoExecutionException( e.getMessage(), e );
                }
            }

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
