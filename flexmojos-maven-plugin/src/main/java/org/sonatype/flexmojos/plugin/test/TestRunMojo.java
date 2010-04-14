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
package org.sonatype.flexmojos.plugin.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.reporting.html.HTMLReport;
import net.sourceforge.cobertura.reporting.xml.SummaryXMLReport;
import net.sourceforge.cobertura.reporting.xml.XMLReport;
import net.sourceforge.cobertura.util.FileFinder;
import net.sourceforge.cobertura.util.Source;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.flexmojos.test.TestRequest;
import org.sonatype.flexmojos.test.TestRunner;
import org.sonatype.flexmojos.test.TestRunnerException;
import org.sonatype.flexmojos.test.launcher.LaunchFlashPlayerException;
import org.sonatype.flexmojos.test.report.TestCaseReport;
import org.sonatype.flexmojos.test.report.TestCoverageReport;

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
public class TestRunMojo
    extends AbstractMojo
{

    private static final String TEST_INFO = "Tests run: {0}, Failures: {1}, Errors: {2}, Time Elapsed: {3} sec";

    private boolean failures = false;

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

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

    /**
     * @component role="org.sonatype.flexmojos.test.TestRunner"
     */
    private TestRunner testRunner;

    /**
     * Uses instruments the bytecode (using apparat) to create test coverage report. Only the test-swf is affected by
     * this.
     * 
     * @parameter expression="${flex.checkCoverage}"
     */
    public boolean checkCoverage;

    /**
     * Uses instruments the bytecode (using apparat) to create test coverage report. Only the test-swf is affected by
     * this.
     * 
     * @parameter default-value="${project.build.directory}/flexmojos/cobertura.ser"
     * @readonly
     */
    public File coverageData;

    /**
     * Uses instruments the bytecode (using apparat) to create test coverage report. Only the test-swf is affected by
     * this.
     * 
     * @parameter default-value="${project.build.directory}/site/cobertura" expression="${flex.reportDestinationDir}"
     */
    public File reportDestinationDir;

    /**
     * @readonly
     */
    private ProjectData projectData;

    /**
     * The coverage report format. Can be 'html', 'xml' and/or 'summaryXml'. Default value is 'html'.
     * 
     * @parameter
     */
    private List<String> formats = Collections.singletonList( "html" );

    /**
     * @parameter expression="${project.build.sourceEncoding}"
     */
    private String encoding;

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

        if ( checkCoverage )
        {
            List<TestCoverageReport> covers = report.getCoverage();
            for ( TestCoverageReport testCoverageReport : covers )
            {
                // F:\4.x\flexmojos-aggregator\flexmojos-testing\flexmojos-test-harness\target\projects\concept\flexunit-example_testFlexUnitExample\src;com\adobe\example;Calculator.as
                String cn = testCoverageReport.getClassname();
                // com\adobe\example;Calculator.as
                cn = cn.substring( cn.indexOf( ';' ) + 1 );
                cn = cn.substring( 0, cn.indexOf( '.' ) );
                cn = cn.replace( '/', '.' ).replace( '\\', '.' ).replace( ';', '.' );

                ClassData classData = this.projectData.getOrCreateClassData( cn );
                for ( Integer touch : testCoverageReport.getTouchs() )
                {
                    classData.touch( touch );
                }
            }
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

    protected void run()
        throws MojoExecutionException, MojoFailureException
    {
        DirectoryScanner scan = new DirectoryScanner();
        scan.setIncludes( new String[] { "*.swf" } );
        scan.addDefaultExcludes();
        scan.setBasedir( testOutputDirectory );
        scan.scan();

        if ( checkCoverage )
        {
            this.projectData = ProjectData.getGlobalProjectData();
        }

        try
        {
            String[] swfs = scan.getIncludedFiles();
            for ( String swfName : swfs )
            {
                try
                {
                    TestRequest testRequest = new TestRequest();
                    testRequest.setTestControlPort( testControlPort );
                    testRequest.setTestPort( testPort );
                    testRequest.setSwf( new File( testOutputDirectory, swfName ) );
                    testRequest.setAllowHeadlessMode( allowHeadlessMode );
                    testRequest.setFlashplayerCommand( flashPlayerCommand );
                    testRequest.setTestTimeout( testTimeout );
                    testRequest.setFirstConnectionTimeout( firstConnectionTimeout );

                    List<String> results = testRunner.run( testRequest );
                    for ( String result : results )
                    {
                        writeTestReport( result );
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
            if ( checkCoverage )
            {
                CoverageDataFileHandler.saveCoverageData( projectData, coverageData );

                FileFinder finder = new FileFinder()
                {
                    public Source getSource( String fileName )
                    {
                        Source source = super.getSource( fileName.replace( ".java", ".as" ) );
                        if ( source == null )
                        {
                            source = super.getSource( fileName.replace( ".java", ".mxml" ) );
                        }
                        return source;
                    }
                };
                finder.addSourceDirectory( project.getBuild().getSourceDirectory() );

                ComplexityCalculator complexity = new ComplexityCalculator( finder );
                try
                {
                    if ( formats.contains( "html" ) )
                    {
                        if(StringUtils.isEmpty( encoding )) {
                            encoding = "UTF-8";
                        }
                        new HTMLReport( projectData, reportDestinationDir, finder, complexity, encoding );
                    }
                    else if ( formats.contains( "xml" ) )
                    {
                        new XMLReport( projectData, reportDestinationDir, finder, complexity );
                    }
                    else if ( formats.contains( "summaryXml" ) )
                    {
                        new SummaryXMLReport( projectData, reportDestinationDir, finder, complexity );
                    }
                }
                catch ( Exception e )
                {
                    throw new MojoExecutionException( "Unable to write coverage report", e );
                }

            }

        }
    }

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

}
