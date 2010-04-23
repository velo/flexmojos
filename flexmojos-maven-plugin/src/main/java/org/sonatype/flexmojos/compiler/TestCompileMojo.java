/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.sonatype.flexmojos.common.FlexExtension;
import org.sonatype.flexmojos.utilities.MavenUtils;

import flex2.tools.oem.Application;
import flex2.tools.oem.Report;

/**
 * Goal to compile the Flex test sources.
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 1.0
 * @goal test-compile
 * @requiresDependencyResolution test
 * @phase test
 */
public class TestCompileMojo
    extends SwfMojo
{

    private static final String ONCE = "once";

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
     * Set this to 'true' to bypass unit tests entirely. Its use is NOT RECOMMENDED, but quite convenient on occasion.
     * 
     * @parameter expression="${maven.test.skip}"
     */
    private boolean skipTests;

    private List<String> testClasses;

    /**
     * Socket connect port for flex/java communication to control if flashplayer is alive
     * 
     * @parameter default-value="13540" expression="${testControlPort}"
     */
    private int testControlPort;

    /**
     * @parameter expression="${project.build.testOutputDirectory}"
     * @readonly
     */
    private File testOutputDirectory;

    /**
     * Socket connect port for flex/java communication to transfer tests results
     * 
     * @parameter default-value="13539" expression="${testPort}"
     */
    private int testPort;

    /**
     * @parameter
     */
    private File testRunnerTemplate;

    /**
     * @parameter expression="${project.build.testSourceDirectory}"
     * @readonly
     */
    private File testSourceDirectory;

    @Override
    protected void attachArtifact()
    {
        // do not attach TestRunner
    }

    private void buildTest( String testFilename, List<? extends String> testClasses )
        throws MojoExecutionException, MojoFailureException
    {
        if ( testClasses == null || testClasses.isEmpty() )
        {
            return;
        }

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

        Application testBuilder;
        try
        {
            testBuilder = new Application( testMxml );
        }
        catch ( FileNotFoundException e )
        {
            // Shouldn't happen
            throw new MojoFailureException( "Unable to find " + testMxml, e );
        }

        setMavenPathResolver( testBuilder );
        testBuilder.setConfiguration( configuration );
        testBuilder.setLogger( new MavenLogger( getLog() ) );
        File testSwf = new File( testOutputDirectory, testFilename + "." + FlexExtension.SWF );
        testBuilder.setOutput( testSwf );

        callCompiler( testBuilder, false );

        updateSecuritySandbox( testSwf );
    }

    @Override
    protected void callDependencyResolver()
        throws MojoExecutionException
    {
        configuration.setExternalLibraryPath( getGlobalDependency() );

        // Set all dependencies as merged
        configuration.setLibraryPath( getDependenciesPath( "compile" ) );
        configuration.addLibraryPath( getDependenciesPath( "merged" ) );
        configuration.addLibraryPath( merge( getResourcesBundles( getToolLocale() ),
                                             getResourcesBundles( runtimeLocales ),
                                             getResourcesBundles( compiledLocales ) ) );

        File[] testFiles =
            new File[] { getFlexmojosTestArtifact( "flexmojos-unittest-support" ).getFile(),
                getFlexmojosUnittestFrameworkIntegrationLibrary().getFile() };

        // and add test libraries
        configuration.includeLibraries( merge( getDependenciesPath( "internal" ), getDependenciesPath( "test" ),
                                               getDependenciesPath( "rsl" ), getDependenciesPath( "caching" ),
                                               getDependenciesPath( "external" ), testFiles ) );

        callThemesResolver();
    }

    @Override
    protected void compileModules()
        throws MojoExecutionException
    {
        // modules are ignored on unit tests
    }

    @Override
    protected void configure()
        throws MojoExecutionException
    {
        compiledLocales = getLocales();
        runtimeLocales = null;

        super.configure();

        // test launcher is at testOutputDirectory
        configuration.addSourcePath( new File[] { testOutputDirectory } );
        configuration.addSourcePath( getValidSourceRoots( project.getTestCompileSourceRoots() ).toArray( new File[0] ) );
        if ( getResource( compiledLocales[0] ) != null )
        {
            configuration.addSourcePath( new File[] { new File( resourceBundlePath ) } );
        }

        configuration.allowSourcePathOverlap( true );

        configuration.enableDebugging( true, super.debugPassword );
    }

    @Override
    protected void configureExterns()
        throws MojoExecutionException
    {
        configuration.setExterns( (File[]) null );
        configuration.setExterns( (String[]) null );
    }

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( skipTests )
        {
            getLog().warn( "Skipping test phase." );
            return;
        }
        else if ( !testSourceDirectory.exists() )
        {
            getLog().warn( "Test folder not found" + testSourceDirectory );
            return;
        }

        setUp();
        run();
        tearDown();
    }

    private File generateTester( List<? extends String> testClasses, String testFilename )
        throws Exception
    {
        // can't use velocity, got:
        // java.io.InvalidClassException:
        // org.apache.velocity.runtime.parser.node.ASTprocess; class invalid for
        // deserialization

        StringBuilder imports = new StringBuilder();

        for ( String testClass : testClasses )
        {
            imports.append( "import " );
            imports.append( testClass );
            imports.append( ";" );
            imports.append( '\n' );
        }

        StringBuilder classes = new StringBuilder();

        for ( String testClass : testClasses )
        {
            testClass = testClass.substring( testClass.lastIndexOf( '.' ) + 1 );
            classes.append( "addTest( " );
            classes.append( testClass );
            classes.append( ");" );
            classes.append( '\n' );
        }

        InputStream templateSource = getTemplate();
        String sourceString = IOUtils.toString( templateSource );
        sourceString = sourceString.replace( "$imports", imports );
        sourceString = sourceString.replace( "$testClasses", classes );
        sourceString = sourceString.replace( "$port", String.valueOf( testPort ) );
        sourceString = sourceString.replace( "$controlPort", String.valueOf( testControlPort ) );
        File testSourceFile = new File( testOutputDirectory, testFilename + ".mxml" );
        FileWriter fileWriter = new FileWriter( testSourceFile );
        IOUtils.write( sourceString, fileWriter );
        fileWriter.flush();
        fileWriter.close();
        return testSourceFile;
    }

    private Artifact getFlexmojosTestArtifact( String artifactId )
        throws MojoExecutionException
    {
        Artifact artifact =
            artifactFactory.createArtifact( "org.sonatype.flexmojos", artifactId, MavenUtils.getFlexMojosVersion(),
                                            "test", "swc" );
        try
        {
            resolver.resolve( artifact, remoteRepositories, localRepository );
        }
        catch ( AbstractArtifactResolutionException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }

        return artifact;
    }

    @SuppressWarnings( "unchecked" )
    private Artifact getFlexmojosUnittestFrameworkIntegrationLibrary()
        throws MojoExecutionException
    {
        Artifact artifact;

        Map<String, Artifact> artifacts = ArtifactUtils.artifactMapByVersionlessId( getDependencyArtifacts() );

        Artifact flexunit = (Artifact) artifacts.get( "com.adobe.flexunit:flexunit" );
        if ( flexunit != null )
        {
            if ( flexunit.getVersion().startsWith( "0" ) )
            {
                // non-flexunit4
                artifact = getFlexmojosTestArtifact( "flexmojos-unittest-flexunit" );
            }
            else
            {
                artifact = getFlexmojosTestArtifact( "flexmojos-unittest-flexunit4" );
            }
        }
        else if ( artifacts.containsKey( "advancedflex:debugger" ) )
        {
            artifact = getFlexmojosTestArtifact( "flexmojos-unittest-advancedflex" );
        }
        else if ( artifacts.containsKey( "com.asunit:asunit" ) )
        {
            artifact = getFlexmojosTestArtifact( "flexmojos-unittest-asunit" );
        }
        else if ( artifacts.containsKey( "net.digitalprimates:fluint" ) )
        {
            artifact = getFlexmojosTestArtifact( "flexmojos-unittest-fluint" );
        }
        else if ( artifacts.containsKey( "org.funit:funit" ) )
        {
            artifact = getFlexmojosTestArtifact( "flexmojos-unittest-funit" );
        }
        else
        {
            throw new MojoExecutionException( "Not found any compatible unit test framework: " + artifacts.keySet()
                + "\n\thttp://docs.sonatype.org/display/FLEXMOJOS/Running+unit+tests" );
        }

        return artifact;
    }

    private String[] getLocales()
    {
        if ( runtimeLocales == null && compiledLocales == null )
        {
            return new String[] { getToolLocale() };
        }
        Set<String> locales = new LinkedHashSet<String>();

        if ( runtimeLocales != null )
        {
            locales.addAll( Arrays.asList( runtimeLocales ) );
        }
        if ( compiledLocales != null )
        {
            locales.addAll( Arrays.asList( compiledLocales ) );
        }

        return locales.toArray( new String[locales.size()] );
    }

    @Override
    protected File getOutput()
    {
        return new File( testOutputDirectory, "TestRunner.swf" );
    }

    private File getResource( String locale )
    {
        return MavenUtils.getLocaleResourcePath( resourceBundlePath, locale );
    }

    private InputStream getTemplate()
        throws MojoExecutionException
    {
        if ( testRunnerTemplate == null )
        {
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
        getLog().debug(
                        "Scanning for tests at " + testSourceDirectory + " for " + Arrays.toString( includeTestFiles )
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
    public void run()
        throws MojoExecutionException, MojoFailureException
    {
        // shouldn't call super super.run();

        if ( ONCE.equals( forkMode ) )
        {
            String testFilename = "TestRunner";
            buildTest( testFilename, testClasses );
        }
        else
        {
            for ( String testClass : testClasses )
            {
                String testFilename = testClass.replaceAll( "[^A-Za-z0-9]", "_" ) + "_Flexmojos_test";
                buildTest( testFilename, Collections.singletonList( testClass ) );
            }
        }

    }

    @Override
    public void setUp()
        throws MojoExecutionException, MojoFailureException
    {
        linkReport = false;

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

        this.testClasses = getTestClasses();

        // workaround to avoid file not found issues.
        super.source = project.getFile();

        super.setUp();
    }

    @Override
    protected void writeReport( Report report, String type )
        throws MojoExecutionException
    {
    }

}
