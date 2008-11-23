/*
 * Flex-mojos is a set of maven plugins to allow maven users to compile, optimize, test and ... Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008 Marvin Herman Froeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.rvin.flexmojo.test;

import info.rvin.flexmojos.utilities.MavenUtils;
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
        getLog().info(
                       "Flex-mojos " + MavenUtils.getFlexMojosVersion()
                           + " - GNU GPL License (NO WARRANTY) - See COPYRIGHT file" );

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
        throws MojoExecutionException, MojoFailureException
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
