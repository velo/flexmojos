package org.sonatype.flexmojos.plugin.test;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.artifactId;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.groupId;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.scope;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.type;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.version;
import static org.sonatype.flexmojos.plugin.common.FlexClassifier.LINK_REPORT;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWC;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.XML;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.CACHING;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.COMPILE;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.EXTERNAL;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.INTERNAL;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.MERGED;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.RSL;
import static org.sonatype.flexmojos.plugin.common.FlexScopes.TEST;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.sonatype.flexmojos.compiler.IRuntimeSharedLibraryPath;
import org.sonatype.flexmojos.compiler.MxmlcConfigurationHolder;
import org.sonatype.flexmojos.compiler.command.Result;
import org.sonatype.flexmojos.plugin.common.flexbridge.MavenPathResolver;
import org.sonatype.flexmojos.plugin.compiler.MxmlcMojo;
import org.sonatype.flexmojos.plugin.test.scanners.FlexClassScanner;
import org.sonatype.flexmojos.plugin.utilities.MavenUtils;
import org.sonatype.flexmojos.util.CollectionUtils;
import org.sonatype.flexmojos.util.PathUtil;
import org.sonatype.flexmojos.util.SocketUtil;

import flex2.compiler.common.SinglePathResolver;

/**
 * <p>
 * Goal which compiles the Flex test into a runnable application
 * </p>
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 1.0
 * @goal test-compile
 * @requiresDependencyResolution test
 * @phase test-compile
 * @configurator flexmojos
 * @threadSafe
 */
public class TestCompilerMojo
    extends MxmlcMojo
{

    public static final String FLEXMOJOS_TEST_CONTROL_PORT = "flexmojos_test_control_port";

    protected static final String FLEXMOJOS_TEST_PORT = "flexmojos_test_port";

    private static final String ONCE = "once";

    /**
     * Uses instruments the bytecode (using apparat) to create test coverage report. Only the test-swf is affected by
     * this.
     * 
     * @parameter expression="${flex.coverage}"
     */
    private boolean coverage;

    /**
     * Classes that shouldn't be include on code coverage analysis.
     * 
     * @parameter
     */
    private String[] coverageExclusions;

    /**
     * The strategy used by flexmojos do determine which files should be taken into account when calculating code
     * coverage. So far there are 4 valid values: 'all', 'disabled', 'link-report' and 'as3Content' <li>
     * <ul>
     * 'all' is the default implementation, includes all .as and .mxml available on source folders, is the fastest but
     * has potential problems with as3 inclusion. This ensures all classes available on source folder are taken into
     * account when calculating code coverage.
     * </ul>
     * <ul>
     * 'disabled' it will produce wrong coverage reports
     * </ul>
     * <ul>
     * 'link-report' will use the application link-report in other to know which classes need to be included on coverage
     * reports, basically mean that all files on your swf/swc will be on the coverage report, but not necessarily all
     * files present on your source folders.
     * </ul>
     * <ul>
     * 'as3Content' will scan all .as and .mxml file contents and will handle they properly, this ensures all classes
     * available on source folder are taken into account when calculating code coverage.
     * </ul>
     * </li>
     * 
     * @parameter default-value="all" expression="${flex.coverageStrategy}"
     */
    private String coverageStrategy;

    /**
     * Files to exclude from testing. If not defined, assumes no exclusions
     * 
     * @parameter
     */
    private String[] excludeTestFiles;

    /**
     * Option to specify the forking mode. Can be "once" or "always". Always fork flashplayer per test class.
     * 
     * @parameter default-value="once" expression="${forkMode}"
     */
    private String forkMode;

    /**
     * File to be tested. If not defined assumes Test*.as and *Test.as
     * 
     * @parameter
     */
    private String[] includeTestFiles;

    /**
     * @readonly
     */
    private FlexClassScanner scanner;

    /**
     * @component role='org.sonatype.flexmojos.plugin.test.scanners.FlexClassScanner'
     */
    private Map<String, FlexClassScanner> scanners;

    /**
     * Set this to 'true' to bypass unit tests entirely. Its use is NOT RECOMMENDED, but quite convenient on occasion.
     * 
     * @parameter expression="${maven.test.skip}"
     */
    private boolean skipTests;

    /**
     * Specify this parameter to run individual tests by file name, overriding the includes/excludes parameters. Each
     * pattern you specify here will be used to create an include pattern formatted like **\/${test}.as and
     * **\/${test}.mxml, so you can just type "-Dtest=MyTest" to run a single test called "foo/MyTest.as" and
     * "bar/MyTest.mxml".
     * <p>
     * This mimic surefire test configuration. <a
     * href="http://maven.apache.org/plugins/maven-surefire-plugin/test-mojo.html#test"
     * >http://maven.apache.org/plugins/maven-surefire-plugin/test-mojo.html#test</a>
     * </p>
     * 
     * @parameter expression="${test}"
     */
    private String test;

    /**
     * The maven compile source roots
     * <p>
     * Equivalent to -compiler.source-path
     * </p>
     * List of path elements that form the roots of ActionScript class
     * 
     * @parameter expression="${project.testCompileSourceRoots}"
     * @required
     * @readonly
     */
    private List<String> testCompileSourceRoots;

    /**
     * @parameter expression="${project.build.testOutputDirectory}"
     * @required
     * @readonly
     */
    private File testOutputDirectory;

    /**
     * The maven test resources
     * 
     * @parameter expression="${project.build.testResources}"
     * @required
     * @readonly
     */
    protected List<Resource> testResources;

    /**
     * @parameter
     */
    private File testRunnerTemplate;

    /**
     * @parameter expression="${project.build.testSourceDirectory}"
     * @readonly
     */
    private File testSourceDirectory;

    private Result buildTest( String testFilename, List<? extends String> testClasses )
        throws MojoExecutionException, MojoFailureException
    {
        getLog().info( "Compiling test class: " + testClasses );

        File testMxml;
        try
        {
            testMxml = generateTester( testClasses, testFilename );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Unable to generate tester class.", e );
        }

        TestCompilerMojo cfg = this.clone();
        cfg.finalName = testFilename;

        return executeCompiler( new MxmlcConfigurationHolder( cfg, testMxml ), fullSynchronization );
    }

    @Override
    public TestCompilerMojo clone()
    {
        return (TestCompilerMojo) super.clone();
    }

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( skipTests )
        {
            getLog().warn( "Skipping test phase." );
            return;
        }

        if ( !PathUtil.existAll( testCompileSourceRoots ) )
        {
            getLog().warn( "Skipping compiler, test source path doesn't exist." );
            return;
        }

        if ( test != null )
        {
            includeTestFiles = new String[] { test };
            excludeTestFiles = null;
        }

        if ( includeTestFiles == null || includeTestFiles.length == 0 )
        {
            includeTestFiles = new String[] { "**/Test*.as", "**/*Test.as", "**/Test*.mxml", "**/*Test.mxml" };
        }
        else
        {
            for ( int i = 0; i < includeTestFiles.length; i++ )
            {
                String pattern = includeTestFiles[i];

                if ( !pattern.endsWith( ".as" ) && !pattern.endsWith( ".mxml" ) )
                {
                    pattern = pattern + ".as";
                }
                includeTestFiles[i] = "**/" + pattern;
            }
        }

        if ( !testOutputDirectory.exists() )
        {
            testOutputDirectory.mkdirs();
        }

        List<String> testClasses = getTestClasses();
        if ( testClasses == null || testClasses.isEmpty() )
        {
            getLog().warn( "Skipping test compiler, no test class found." );
            return;
        }

        if ( ONCE.equals( forkMode ) )
        {
            String testFilename = "TestRunner";
            checkResult( buildTest( testFilename, testClasses ) );
        }
        else
        {
            List<Result> results = new ArrayList<Result>();

            for ( String testClass : testClasses )
            {
                String testFilename = testClass.replaceAll( "[^A-Za-z0-9]", "_" ) + "_Flexmojos_test";
                results.add( buildTest( testFilename, Collections.singletonList( testClass ) ) );
            }

            wait( results );
        }
    }

    private File generateTester( List<? extends String> testClasses, String testFilename )
        throws Exception
    {
        // can't use velocity, got:
        // java.io.InvalidClassException:
        // org.apache.velocity.runtime.parser.node.ASTprocess; class invalid for
        // deserialization

        StringBuilder imports = getImports( testClasses );
        StringBuilder includes = getExtraIncludes( testOutputDirectory );
        StringBuilder classes = getClasses( testClasses );

        Integer testControlPort = SocketUtil.freePort();
        putPluginContext( FLEXMOJOS_TEST_CONTROL_PORT, testControlPort );
        Integer testPort = SocketUtil.freePort();
        putPluginContext( FLEXMOJOS_TEST_PORT, testPort );
        getLog().debug( "Flexmojos test port: " + testPort + " - control: " + testControlPort );

        InputStream templateSource = getTemplate();
        String sourceString = IOUtils.toString( templateSource );
        sourceString = sourceString.replace( "$imports", imports );
        sourceString = sourceString.replace( "$includes", includes );
        sourceString = sourceString.replace( "$testClasses", classes );
        sourceString = sourceString.replace( "$port", testPort.toString() );
        sourceString = sourceString.replace( "$controlPort", String.valueOf( testControlPort ) );
        File testSourceFile = new File( testOutputDirectory, testFilename + ".mxml" );
        FileWriter fileWriter = new FileWriter( testSourceFile );
        IOUtils.write( sourceString, fileWriter );
        fileWriter.flush();
        fileWriter.close();
        return testSourceFile;
    }

    private StringBuilder getClasses( List<? extends String> testClasses )
    {
        StringBuilder classes = new StringBuilder();

        for ( String testClass : testClasses )
        {
            testClass = testClass.substring( testClass.lastIndexOf( '.' ) + 1 );
            classes.append( "addTest( " );
            classes.append( testClass );
            classes.append( ");" );
            classes.append( '\n' );
        }
        return classes;
    }

    @Override
    public Boolean getDebug()
    {
        return true;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public File[] getExternalLibraryPath()
    {
        return MavenUtils.getFiles( getGlobalArtifact() );
    }

    private StringBuilder getExtraIncludes( File testOutputDirectory )
    {
        StringBuilder includes = new StringBuilder();
        if ( coverage )
        {
            FlexClassScanner scanner = getFlexClassScanner();
            for ( String snippet : scanner.getAs3Snippets() )
            {
                // as3 include "sampleInclude.as";
                // includes.append( "include \"" );
                // includes.append( snippets.replace( '\\', '/' ) );
                // includes.append( "\";\n" );

                // mxml <mx:Script source="includes/CameraExample.as" />
                includes.append( "<mx:Script source=\"" );
                includes.append( snippet.replace( '\\', '/' ) );
                includes.append( "\" />\n" );

            }
        }
        return includes;
    }

    private FlexClassScanner getFlexClassScanner()
    {
        if ( this.scanner == null )
        {
            File[] sp = getSourcePath();

            scanner = scanners.get( coverageStrategy );
            if ( scanner == null )
            {
                throw new IllegalArgumentException( "Invalid coverageFlexClassScanner: '" + coverageStrategy + "'" );
            }

            Map<String, Object> context = new LinkedHashMap<String, Object>();
            // TODO need a better idea to resolve link report file
            context.put( LINK_REPORT, new File( project.getBuild().getDirectory(), project.getBuild().getFinalName()
                + "-" + LINK_REPORT + "." + XML ) );
            scanner.scan( sp, coverageExclusions, context );
        }
        return scanner;
    }

    protected Artifact getFlexmojosTestArtifact( String artifactId )
    {
        return getFlexmojosTestArtifact( artifactId, null );
    }

    protected Artifact getFlexmojosTestArtifact( String artifactId, String classifier )
    {
        Artifact artifact =
            resolve( "org.sonatype.flexmojos", artifactId, MavenUtils.getFlexMojosVersion(), classifier, "swc" );

        return artifact;
    }

    @SuppressWarnings( "unchecked" )
    protected Artifact getFlexmojosUnittestFrameworkIntegrationLibrary()
    {

        if ( getDependency( groupId( "com.adobe.flexunit" ), artifactId( "flexunit" ), version( startsWith( "0" ) ) ) != null )
        {
            return getFlexmojosTestArtifact( "flexmojos-unittest-flexunit" );
        }
        else if ( getDependency( groupId( "com.adobe.flexunit" ), artifactId( "flexunit" ) ) != null )
        {
            return getFlexmojosTestArtifact( "flexmojos-unittest-flexunit4" );
        }
        else if ( getDependency( groupId( "org.flexunit" ), artifactId( "flexunit" ) ) != null )
        {
            return getFlexmojosTestArtifact( "flexmojos-unittest-flexunit4" );
        }
        else if ( getDependency( groupId( "advancedflex" ), artifactId( "debugger" ) ) != null )
        {
            return getFlexmojosTestArtifact( "flexmojos-unittest-advancedflex" );
        }
        else if ( getDependency( groupId( "com.asunit" ), artifactId( "asunit" ) ) != null )
        {
            return getFlexmojosTestArtifact( "flexmojos-unittest-asunit" );
        }
        else if ( getDependency( groupId( "net.digitalprimates" ), artifactId( "fluint" ) ) != null )
        {
            return getFlexmojosTestArtifact( "flexmojos-unittest-fluint" );
        }
        else if ( getDependency( groupId( "org.funit" ), artifactId( "funit" ) ) != null )
        {
            return getFlexmojosTestArtifact( "flexmojos-unittest-funit" );
        }
        else
        {
            if ( getLog().isDebugEnabled() )
            {
                getLog().debug( "Unable to find test dependency among this: " + getDependencies() );
            }

            throw new IllegalStateException( "Not found any compatible unit test framework"
                + "\n\thttp://docs.sonatype.org/display/FLEXMOJOS/Running+unit+tests" );
        }
    }

    private Artifact getFlexmojosUnittestSupport()
    {
        return getFlexmojosTestArtifact( "flexmojos-unittest-support", getIsAirProject() ? "air" : "flex" );
    }

    private StringBuilder getImports( List<? extends String> testClasses )
    {
        StringBuilder imports = new StringBuilder();

        for ( String testClass : testClasses )
        {
            imports.append( "import " );
            imports.append( testClass );
            imports.append( "; " );
            if ( testClass.indexOf( '.' ) != -1 )
            {
                imports.append( testClass.substring( testClass.lastIndexOf( '.' ) + 1 ) );
            }
            else
            {
                imports.append( testClass );
            }
            imports.append( ";" );
            imports.append( '\n' );
        }
        return imports;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public File[] getIncludeLibraries()
    {
        Collection<Artifact> coverArtifact =
            (Collection<Artifact>) ( coverage ? Collections.singletonList( getFlexmojosTestArtifact( "flexmojos-test-coverage" ) )
                            : Collections.emptyList() );
        return MavenUtils.getFiles( coverArtifact,
                                    Collections.singletonList( getFlexmojosUnittestSupport() ),
                                    Collections.singletonList( getFlexmojosUnittestFrameworkIntegrationLibrary() ),
                                    getDependencies( type( SWC ),//
                                                     anyOf( scope( INTERNAL ), scope( RSL ), scope( CACHING ),
                                                            scope( TEST ) ),//
                                                     not( GLOBAL_MATCHER ) ) );
    }

    @Override
    public List<String> getIncludes()
    {
        if ( !coverage )
        {
            return super.getIncludes();
        }

        List<String> includes = super.getIncludes();
        if ( includes == null )
        {
            includes = new ArrayList<String>();
        }
        else
        {
            includes = new ArrayList<String>( includes );
        }

        FlexClassScanner scanner = getFlexClassScanner();

        for ( String testFile : scanner.getAs3Classes() )
        {
            String include = toClass( testFile );
            includes.add( include );
        }

        return includes;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public File[] getLibraryPath()
    {
        return MavenUtils.getFiles( getDependencies( type( SWC ),//
                                                     anyOf( scope( MERGED ), scope( EXTERNAL ), scope( COMPILE ),
                                                            scope( nullValue( String.class ) ) ),//
                                                     not( GLOBAL_MATCHER ) ),//
                                    getCompiledResouceBundles() );
    }

    @Override
    public String[] getLoadExterns()
    {
        return null;
    }

    @Override
    public String[] getLocale()
    {
        return CollectionUtils.merge( super.getLocalesRuntime(), super.getLocale() );
    }

    @Override
    public String[] getLocalesRuntime()
    {
        return null;
    }

    public SinglePathResolver getMavenPathResolver()
    {
        List<Resource> resources = new ArrayList<Resource>();
        resources.addAll( this.testResources );
        resources.addAll( this.resources );
        return new MavenPathResolver( resources );
    }

    @Override
    public Boolean getOptimize()
    {
        return false;
    }

    @Override
    public String getOutput()
    {
        return PathUtil.path( new File( getTargetDirectory(), getFinalName() + "." + getProjectType() ) );
    }

    @Override
    public IRuntimeSharedLibraryPath[] getRuntimeSharedLibraryPath()
    {
        return null;
    }

    @Override
    protected File getSourceFile()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public File[] getSourcePath()
    {
        Set<File> files = new LinkedHashSet<File>();

        files.addAll( PathUtil.existingFilesList( testCompileSourceRoots ) );
        files.addAll( Arrays.asList( super.getSourcePath() ) );

        return files.toArray( new File[0] );
    }

    @Override
    public File getTargetDirectory()
    {
        testOutputDirectory.mkdirs();
        return testOutputDirectory;
    }

    public InputStream getTemplate()
        throws MojoExecutionException
    {
        if ( testRunnerTemplate == null )
        {
            if ( getIsAirProject() )
            {
                return getClass().getResourceAsStream( "/templates/test/Air-TestRunner.vm" );
            }
            return getClass().getResourceAsStream( "/templates/test/TestRunner.vm" );
        }
        else if ( !testRunnerTemplate.exists() )
        {
            throw new MojoExecutionException( "Template file not found: " + testRunnerTemplate );
        }
        else
        {
            try
            {
                return new FileInputStream( testRunnerTemplate );
            }
            catch ( FileNotFoundException e )
            {
                // Never should happen
                throw new MojoExecutionException( "Error reading template file", e );
            }
        }
    }

    private List<String> getTestClasses()
    {
        getLog().debug( "Scanning for tests at " + testSourceDirectory + " for " + Arrays.toString( includeTestFiles )
                            + " but " + Arrays.toString( excludeTestFiles ) );

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes( includeTestFiles );
        scanner.setExcludes( excludeTestFiles );
        scanner.addDefaultExcludes();
        scanner.setBasedir( testSourceDirectory );
        scanner.scan();

        getLog().debug( "Test files: " + Arrays.toString( scanner.getIncludedFiles() ) );
        List<String> testClasses = new ArrayList<String>();
        for ( String testClass : scanner.getIncludedFiles() )
        {
            int endPoint = testClass.lastIndexOf( '.' );
            testClass = testClass.substring( 0, endPoint ); // remove extension
            testClass = testClass.replace( '/', '.' ); // Unix OS
            testClass = testClass.replace( '\\', '.' ); // Windows OS
            testClasses.add( testClass );
        }
        getLog().debug( "Test classes: " + testClasses );
        return testClasses;
    }

    @Override
    public boolean isUpdateSecuritySandbox()
    {
        // not optional for tests, flexmojos needs sandbox security disabled
        return true;
    }
}
