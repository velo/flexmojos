/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.flexcover;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.sonatype.flexmojos.test.FlexUnitMojo;

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
     * Full path to CoverageViewer application, should end with CoverageViewer.exe on Windows or
     * CoverageViewer.app/Contents/MacOS/CoverageViewer on Mac or ?? on Linux
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
        String outputPath = new File( build.getTestOutputDirectory() ).getAbsolutePath();
        cl.addArguments( new String[] { "-output", outputPath + File.separator + "flexcover-results.cvr",
            outputPath + File.separator + "TestRunner.cvm" } );
        getLog().info( "launching CoverageViewer: " + cl.toString() );
        Process p = null;
        try
        {
            p = cl.execute();
            Thread.sleep( 3000 );
            // CommandLineUtils.executeCommandLine( cl , new CommandLineUtils.StringStreamConsumer(), new
            // CommandLineUtils.StringStreamConsumer() );
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

        // FIXME swf = new File( "target/flexcover-classes", "TestRunner.swf" );

        super.run();

        if ( p != null )
        {
            p.destroy();
        }
    }

}
