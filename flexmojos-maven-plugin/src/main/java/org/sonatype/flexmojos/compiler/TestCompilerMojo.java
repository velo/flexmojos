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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.sonatype.flexmojos.utilities.MavenUtils;

/**
 * Goal to compile the Flex test sources.
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 1.0
 * @goal test-compile
 * @requiresDependencyResolution
 * @phase test
 */
public class TestCompilerMojo
    extends ApplicationMojo
{

    /**
     * Set this to 'true' to bypass unit tests entirely. Its use is NOT RECOMMENDED, but quite convenient on occasion.
     * 
     * @parameter expression="${maven.test.skip}"
     */
    private boolean skipTests;

    /**
     * @parameter
     */
    private File testRunnerTemplate;

    /**
     * File to be tested. If not defined assumes Test*.as and *Test.as
     * 
     * @parameter
     */
    private String[] includeTestFiles;

    /**
     * Files to exclude from testing. If not defined, assumes no exclusions
     * 
     * @parameter
     */
    private String[] excludeTestFiles;

    /**
     * @parameter expression="${project.build.testSourceDirectory}"
     * @readonly
     */
    private File testFolder;

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

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().info(
                       "flexmojos " + MavenUtils.getFlexMojosVersion()
                           + " - GNU GPL License (NO WARRANTY) - See COPYRIGHT file" );

        if ( skipTests )
        {
            getLog().warn( "Skipping test phase." );
            return;
        }
        else if ( !testFolder.exists() )
        {
            getLog().warn( "Test folder not found" + testFolder );
            return;
        }

        setUp();
        run();
        tearDown();
    }

    @Override
    public void setUp()
        throws MojoExecutionException, MojoFailureException
    {
        isSetProjectFile = false;
        linkReport = false;
        loadExterns = null;

        if ( includeTestFiles == null || includeTestFiles.length == 0 )
        {
            includeTestFiles = new String[] { "**/Test*.as", "**/*Test.as" };
        }
        else
        {
            for ( int i = 0; i < includeTestFiles.length; i++ )
            {
                String pattern = includeTestFiles[i];
                if ( pattern.endsWith( ".java" ) )
                {
                    pattern = pattern.substring( 0, pattern.length() - 5 );
                }
                // Allow paths delimited by '.' or '/'
                pattern = pattern.replace( '.', '/' );
                includeTestFiles[i] = "**/" + pattern + ".as";
            }
        }

        File outputFolder = new File( build.getTestOutputDirectory() );
        if ( !outputFolder.exists() )
        {
            outputFolder.mkdirs();
        }

        List<String> testClasses = getTestClasses();

        File testSourceFile;
        try
        {
            testSourceFile = generateTester( testClasses );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Unable to generate tester class.", e );
        }

        sourceFile = null;
        source = testSourceFile;

        super.setUp();
    }

    private List<String> getTestClasses()
    {
        getLog().debug(
                        "Scanning for tests at " + testFolder + " for " + Arrays.toString( includeTestFiles ) + " but "
                            + Arrays.toString( excludeTestFiles ) );

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes( includeTestFiles );
        scanner.setExcludes( excludeTestFiles );
        scanner.addDefaultExcludes();
        scanner.setBasedir( testFolder );
        scanner.scan();

        getLog().debug( "Test files: " + scanner.getIncludedFiles() );
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

    private File generateTester( List<String> testClasses )
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
        File testSourceFile = new File( build.getTestOutputDirectory(), "TestRunner.mxml" );
        FileWriter fileWriter = new FileWriter( testSourceFile );
        IOUtils.write( sourceString, fileWriter );
        fileWriter.flush();
        fileWriter.close();
        return testSourceFile;
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

    @Override
    protected void configure()
        throws MojoExecutionException, MojoFailureException
    {
        compiledLocales = getLocales();
        runtimeLocales = null;

        super.configure();

        // test launcher is at testOutputDirectory
        configuration.addSourcePath( new File[] { new File( build.getTestOutputDirectory() ) } );
        configuration.addSourcePath( getValidSourceRoots( project.getTestCompileSourceRoots() ).toArray( new File[0] ) );
        if ( getResource( compiledLocales[0] ) != null )
        {
            configuration.addSourcePath( new File[] { new File( resourceBundlePath ) } );
        }

        configuration.allowSourcePathOverlap( true );

        configuration.enableDebugging( true, super.debugPassword );
    }

    private File getResource( String locale )
    {
        try
        {
            return MavenUtils.getLocaleResourcePath( resourceBundlePath, locale );
        }
        catch ( MojoExecutionException e )
        {
            return null;
        }
    }

    @Override
    protected void resolveDependencies()
        throws MojoExecutionException, MojoFailureException
    {
        configuration.setExternalLibraryPath( getGlobalDependency() );

        // Set all dependencies as merged
        configuration.setLibraryPath( getDependenciesPath( "compile" ) );
        configuration.addLibraryPath( getDependenciesPath( "merged" ) );
        configuration.addLibraryPath( merge( getResourcesBundles( getDefaultLocale() ),
                                             getResourcesBundles( runtimeLocales ),
                                             getResourcesBundles( compiledLocales ) ) );

        // and add test libraries
        configuration.includeLibraries( merge( getDependenciesPath( "internal" ), getDependenciesPath( "test" ),
                                               getDependenciesPath( "rsl" ), getDependenciesPath( "caching" ),
                                               getDependenciesPath( "external" ) ) );

        configuration.setTheme( getThemes() );
    }

    private String[] getLocales()
    {
        if ( runtimeLocales == null && compiledLocales == null )
        {
            return new String[] { getDefaultLocale() };
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

        return locales.toArray( new String[0] );
    }

    private File[] merge( File[]... filesSets )
    {
        List<File> files = new ArrayList<File>();
        for ( File[] fileSet : filesSets )
        {
            files.addAll( Arrays.asList( fileSet ) );
        }
        return files.toArray( new File[0] );
    }

    @Override
    protected File getOutput()
    {
        return new File( build.getTestOutputDirectory(), "TestRunner.swf" );
    }

    @Override
    protected void compileModules()
        throws MojoFailureException, MojoExecutionException
    {
        // modules are ignored on unit tests
    }
}
