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
 * @requiresDependencyResolution test
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
