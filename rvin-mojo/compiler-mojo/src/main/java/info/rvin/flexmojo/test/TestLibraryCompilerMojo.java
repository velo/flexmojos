package info.rvin.flexmojo.test;

import info.rvin.mojo.flexmojo.compiler.LibraryMojo;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal to compile the Flex test sources.
 * 
 * @goal test-swc
 * @requiresDependencyResolution
 */
public class TestLibraryCompilerMojo
    extends LibraryMojo
{

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        File testFolder = new File( build.getTestSourceDirectory() );

        if ( testFolder.exists() )
        {
            setUp();
            run();
            tearDown();
        }
        else
        {
            getLog().warn( "Test folder not found." );
        }

    }

    @Override
    public void setUp()
        throws MojoExecutionException, MojoFailureException
    {
        isSetProjectFile = false;

        File outputFolder = new File( build.getTestOutputDirectory() );
        if ( !outputFolder.exists() )
        {
            outputFolder.mkdirs();
        }

        outputFile = new File( build.getDirectory(), build.getFinalName() + "-test.swc" );

        includeSources = getValidSourceRoots( project.getTestCompileSourceRoots() ).toArray( new File[0] );

        super.setUp();
    }

    @Override
    protected void configure()
        throws MojoExecutionException
    {
        super.configure();

        // add test libraries
        configuration.addLibraryPath( getDependenciesPath( "test" ) );

        configuration.addSourcePath( getValidSourceRoots( project.getTestCompileSourceRoots() ).toArray( new File[0] ) );
    }

    @Override
    protected void tearDown()
        throws MojoExecutionException, MojoFailureException
    {
        super.tearDown();

        projectHelper.attachArtifact( project, "swc", "test", outputFile );

    }

}
