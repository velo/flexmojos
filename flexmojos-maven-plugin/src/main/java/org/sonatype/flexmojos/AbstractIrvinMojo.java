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
package org.sonatype.flexmojos;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.MavenProjectHelper;
import org.sonatype.flexmojos.utilities.MavenUtils;

/**
 * Encapsulate the access to Maven API. Some times just to hide Java 5 warnings
 */
public abstract class AbstractIrvinMojo
    extends AbstractMojo
{

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * @parameter expression="${project.build}"
     * @required
     * @readonly
     */
    protected Build build;

    /**
     * @component
     */
    protected MavenProjectHelper projectHelper;

    /**
     * @component
     */
    protected ArtifactFactory artifactFactory;

    /**
     * @component
     */
    protected ArtifactResolver resolver;

    /**
     * @component
     */
    protected ArtifactMetadataSource artifactMetadataSource;

    /**
     * @component
     */
    protected MavenProjectBuilder mavenProjectBuilder;

    /**
     * Local repository to be used by the plugin to resolve dependencies.
     * 
     * @parameter expression="${localRepository}"
     */
    protected ArtifactRepository localRepository;

    /**
     * List of remote repositories to be used by the plugin to resolve dependencies.
     * 
     * @parameter expression="${project.remoteArtifactRepositories}"
     */
    @SuppressWarnings( "unchecked" )
    protected List remoteRepositories;

    /**
     * @parameter expression="${plugin.artifacts}"
     */
    protected List<Artifact> pluginArtifacts;

    /**
     * @parameter default-value="false" expression="${flexmojos.skip}"
     */
    private boolean skip;

    /**
     * Construct Mojo instance
     */
    public AbstractIrvinMojo()
    {
        super();
    }

    /**
     * Executes plugin
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( skip )
        {
            getLog().info( "Skipping Flexmojos execution" );
            return;
        }

        getLog().info(
                       "Flexmojos " + MavenUtils.getFlexMojosVersion()
                           + " - Apache License (NO WARRANTY) - See COPYRIGHT file" );
        setUp();
        run();
        tearDown();
    }

    /**
     * Perform setup before plugin is run.
     * 
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    protected abstract void setUp()
        throws MojoExecutionException, MojoFailureException;

    /**
     * Perform plugin functionality
     * 
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    protected abstract void run()
        throws MojoExecutionException, MojoFailureException;

    /**
     * Perform (cleanup) actions after plugin has run
     * 
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    protected abstract void tearDown()
        throws MojoExecutionException, MojoFailureException;

}
