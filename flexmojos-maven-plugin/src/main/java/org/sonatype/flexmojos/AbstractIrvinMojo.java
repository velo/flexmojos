/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
import org.sonatype.flexmojos.common.FlexExtension;
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

    // dependency artifactes
    private Set<Artifact> dependencyArtifacts;

    /**
     * Returns Set of dependency artifacts which are resolved for the project.
     * 
     * @return Set of dependency artifacts.
     * @throws MojoExecutionException
     */
    protected Set<Artifact> getDependencyArtifacts()
        throws MojoExecutionException
    {
        if ( dependencyArtifacts == null )
        {
            dependencyArtifacts =
                MavenUtils.getDependencyArtifacts( project, resolver, localRepository, remoteRepositories,
                                                   artifactMetadataSource, artifactFactory );
        }
        return dependencyArtifacts;
    }

    /**
     * Get dependency artifacts for given scope
     * 
     * @param scopes for which to get artifacts
     * @return List of artifacts
     * @throws MojoExecutionException
     */
    protected List<Artifact> getDependencyArtifacts( String... scopes )
        throws MojoExecutionException
    {
        if ( scopes == null )
            return null;

        if ( scopes.length == 0 )
        {
            return new ArrayList<Artifact>();
        }

        List<String> scopesList = Arrays.asList( scopes );

        List<Artifact> artifacts = new ArrayList<Artifact>();
        for ( Artifact artifact : getDependencyArtifacts() )
        {
            if ( FlexExtension.SWC.equals( artifact.getType() ) && scopesList.contains( artifact.getScope() ) )
            {
                artifacts.add( artifact );
            }
        }
        return artifacts;
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
