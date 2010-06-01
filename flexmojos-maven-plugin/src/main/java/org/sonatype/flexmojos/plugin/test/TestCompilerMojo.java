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

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.not;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.artifactId;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.groupId;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.scope;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.type;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.version;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWC;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.FileSet;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.hamcrest.text.StringStartsWith;
import org.sonatype.flexmojos.compiler.IRuntimeSharedLibraryPath;
import org.sonatype.flexmojos.compiler.MxmlcConfigurationHolder;
import org.sonatype.flexmojos.compiler.command.Result;
import org.sonatype.flexmojos.plugin.compiler.MxmlcMojo;
import org.sonatype.flexmojos.plugin.compiler.flexbridge.MavenPathResolver;
import org.sonatype.flexmojos.plugin.utilities.CollectionUtils;
import org.sonatype.flexmojos.plugin.utilities.MavenUtils;
import org.sonatype.flexmojos.util.PathUtil;

import flex2.compiler.common.SinglePathResolver;

/**
 * <p>
 * Goal which compiles the Flex test into a runnable application
 * </p>
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 4.0
 * @goal test-compile
 * @requiresDependencyResolution test
 * @phase test-compile
 * @configurator flexmojos
 * @threadSafe
 */
public class TestCompilerMojo
    extends MxmlcMojo
{

    private static final String ONCE = "once";

    /**
     * Uses instruments the bytecode (using apparat) to create test coverage report. Only the test-swf is affected by
     * this.
     * 
     * @parameter expression="${flex.coverage}"
     */
    private boolean coverage;

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
     * Socket connect port for flex/java communication to control if flashplayer is alive
     * 
     * @parameter default-value="13540" expression="${testControlPort}"
     */
    private int testControlPort;

    /**
     * @parameter expression="${project.build.testOutputDirectory}"
     * @required
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

        if ( !PathUtil.exist( testCompileSourceRoots ) )
        {
            getLog().warn( "Skipping compiler, test source path doesn't exist." );
            return;
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

        if ( getDependency( groupId( "com.adobe.flexunit" ), artifactId( "flexunit" ),
                            version( StringStartsWith.startsWith( "0" ) ) ) != null )
        {
            return getFlexmojosTestArtifact( "flexmojos-unittest-flexunit" );
        }
        else if ( getDependency( groupId( "com.adobe.flexunit" ), artifactId( "flexunit" ) ) != null )
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
        else if ( getDependency( groupId( "com.adobe.mustella" ), artifactId( "mustella" ) ) != null )
        {
            return getFlexmojosTestArtifact( "flexmojos-unittest-mustella" );
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

    @SuppressWarnings( "unchecked" )
    @Override
    public File[] getIncludeLibraries()
    {
        Collection<Artifact> coverArtifact =
            (Collection<Artifact>) ( coverage ? Collections.singletonList( getFlexmojosTestArtifact( "flexmojos-test-coverage" ) )
                            : Collections.emptyList() );
        return MavenUtils.getFiles( coverArtifact, Collections.singletonList( getFlexmojosUnittestSupport() ),
                                    Collections.singletonList( getFlexmojosUnittestFrameworkIntegrationLibrary() ),
                                    getDependencies( type( SWC ),// 
                                                     anyOf( scope( INTERNAL ), scope( RSL ), scope( CACHING ),
                                                            scope( TEST ) ),//
                                                     not( GLOBAL_MATCHER ) ) );
    }

    @Override
    public List<String> getIncludes()
    {
        List<String> includes = new ArrayList<String>();
        
        File[] sp = getSourcePath();
        List<FileSet> sets = as3ClassesFileSet( sp );
        for ( FileSet fileset : sets )
        {
            DirectoryScanner scan = scan( fileset );
            for ( String testFile : scan.getIncludedFiles() )
            {
                String include = toClass( testFile );
                includes.add(include);
            }
        }    
        
        return includes;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public File[] getLibraryPath()
    {
        return MavenUtils.getFiles( getDependencies( type( SWC ),//
                                                     anyOf( scope( MERGED ), scope( EXTERNAL ), scope( COMPILE ),
                                                            scope( null ) ),//
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
        return CollectionUtils.merge( super.getLocalesRuntime(), super.getLocale() ).toArray( new String[0] );
    }

    @Override
    public String[] getLocalesRuntime()
    {
        return null;
    }

    public SinglePathResolver getMavenPathResolver()
    {
        List<Resource> resources = new ArrayList<Resource>();
        resources.addAll( this.resources );
        resources.addAll( this.testResources );
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
        return PathUtil.getCanonicalPath( new File( getTargetDirectory(), getFinalName() + "." + getProjectType() ) );
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

        files.addAll( PathUtil.getExistingFilesList( testCompileSourceRoots ) );
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
    public boolean isUpdateSecuritySandbox()
    {
        // not optional for tests, flexmojos needs sandbox security disabled
        return true;
    }
}
