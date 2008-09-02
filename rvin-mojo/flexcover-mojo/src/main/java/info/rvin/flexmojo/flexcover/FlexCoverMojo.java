package info.rvin.flexmojo.flexcover;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import info.rvin.flexmojo.test.FlexUnitMojo;

/**
 * Goal which runs a Flex project against the FlexCover CoverageViewer
 *
 * @extendsPlugin flex-compiler-mojo
 * @extendsGoal test-run
 * @goal flexcover-run
 * @phase test
 * @requiresDependencyResolution
 * @requiresProject
 */
public class FlexCoverMojo
    extends FlexUnitMojo
{

    /**
     * Full path to CoverageViewer application, should end with CoverageViewer.exe on Windows
     * or CoverageViewer.app/Contents/MacOS/CoverageViewer on Mac or
     * ?? on Linux
     *
     * @parameter
     * @required
     */
    private String coverageViewerPath;

    @Override
    protected void run()
        throws MojoExecutionException, MojoFailureException
    {
        // start CoverageViewer air app
        Commandline cl = new Commandline( coverageViewerPath );
        String outputPath = new File(build.getTestOutputDirectory()).getAbsolutePath();
        cl.addArguments( new String[] {"-output", outputPath+File.separator+"flexcover-results.cvr", outputPath+File.separator+"TestRunner.cvm"} );
        getLog().info( "launching CoverageViewer: "+cl.toString());
        Process p = null;
        try
        {
            p = cl.execute();
            Thread.sleep( 3000 );
//            CommandLineUtils.executeCommandLine( cl , new CommandLineUtils.StringStreamConsumer(),  new CommandLineUtils.StringStreamConsumer() );
        }
        catch ( CommandLineException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( InterruptedException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        swf = new File( build.getTestOutputDirectory(), "TestRunner.swf" );
        
        super.run();
        
        if (p != null)
        {
            p.destroy();
        }
    }
    
}
