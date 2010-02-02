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
package org.sonatype.flexmojos.compiler;

import static org.sonatype.flexmojos.common.FlexExtension.SWC;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Build a SWC of the test classes for the current project.
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 2.0
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
        File outputFolder = new File( build.getTestOutputDirectory() );
        if ( !outputFolder.exists() )
        {
            outputFolder.mkdirs();
        }

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
    protected void attachArtifact()
    {
        projectHelper.attachArtifact( project, SWC, "test", getOutput() );
    }

    @Override
    protected File getOutput()
    {
        return new File( build.getDirectory(), build.getFinalName() + "-test.swc" );
    }

}
