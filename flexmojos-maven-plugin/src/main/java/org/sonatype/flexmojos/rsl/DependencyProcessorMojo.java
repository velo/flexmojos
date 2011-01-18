/**
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.rsl;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;
import org.apache.maven.shared.artifact.filter.collection.ArtifactIdFilter;
import org.apache.maven.shared.artifact.filter.collection.ClassifierFilter;
import org.apache.maven.shared.artifact.filter.collection.FilterArtifacts;
import org.apache.maven.shared.artifact.filter.collection.GroupIdFilter;
import org.apache.maven.shared.artifact.filter.collection.ProjectTransitivityFilter;
import org.apache.maven.shared.artifact.filter.collection.TypeFilter;
import org.sonatype.flexmojos.MavenMojo;
import org.sonatype.flexmojos.optimizer.OptimizerMojo;

/**
 * Goal that processes project dependencies and copies the matching dependencies
 * to a specified folder. This goal is used to output RSLs where they will be
 * retrieved by the flash player at runtime.
 * 
 * @author Roberto Lo Giacco (rlogiacco@gmail.com)
 * 
 * @goal process-rsl
 * @phase compile
 * @requiresDependencyResolution
 */
public class DependencyProcessorMojo extends OptimizerMojo implements MavenMojo
{
    /**
     * @optional
     * @parameter expression="${excludeTransitive}" default-value="false"
     */
    protected boolean excludeTransitive;

    /**
     * @parameter expression="${project.build.directory}/rsl"
     */
    protected File outputDirectory;

    /**
     * @required
     * @parameter expression="${scope}" default-value="rsl"
     */
    private String scope;

    /**
     * @parameter expression="${rslExpression}" default-value="swf"
     */
    protected String rslExtension;

    /**
     * @parameter expression="${stripVersion}" default-value="false"
     */
    private boolean stripVersion;

    /**
     * @parameter expression="${includeTypes}" default-value="swc"
     * @optional
     */
    private String includeTypes;

    /**
     * @parameter expression="${excludeTypes}" default-value=""
     * @optional
     */
    private String excludeTypes;

    /**
     * @parameter expression="${includeClassifiers}" default-value=""
     * @optional
     */
    private String includeClassifiers;

    /**
     * @parameter expression="${excludeClassifiers}" default-value=""
     * @optional
     */
    private String excludeClassifiers;

    /**
     * @parameter expression="${includeGroupIds}" default-value=""
     * @optional
     */
    private String includeGroupIds;

    /**
     * @parameter expression="${excludeGroupIds}" default-value="com.adobe.*"
     * @optional
     */
    private String excludeGroupIds;

    /**
     * @parameter expression="${includeArtifactIds}" default-value=""
     * @optional
     */
    private String includeArtifactIds;

    /**
     * @parameter expression="${excludeArtifactIds}" default-value=""
     * @optional
     */
    private String excludeArtifactIds;

    /**
     * @component
     * @readonly
     * @required
     */
    protected ArtifactFactory artifactFactory;

    /**
     * @component
     * @readonly
     * @required
     * */
    protected ArtifactResolver resolver;

    /**
     * Location of the local repository.
     * 
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    protected ArtifactRepository localRepository;

    /**
     * List of Remote Repositories used by the resolver
     * 
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    protected List remoteRepositories;

    /**
     * @component
     * @readonly
     * @required
     */
    protected ArtifactInstaller installer;

    /**
     * @component
     * 
     * @readonly
     * @required
     */
    private ArtifactMetadataSource artifactMetadataSource;

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        // add filters in well known order, least specific to most specific
        FilterArtifacts filter = new FilterArtifacts();
        filter.addFilter( new ProjectTransitivityFilter( project
                .getDependencyArtifacts(), this.excludeTransitive ) );
        filter.addFilter( new TypeFilter( this.includeTypes, this.excludeTypes ) );
        filter.addFilter( new ClassifierFilter( this.includeClassifiers,
                this.excludeClassifiers ) );
        filter.addFilter( new GroupIdFilter( this.includeGroupIds,
                this.excludeGroupIds ) );
        filter.addFilter( new ArtifactIdFilter( this.includeArtifactIds,
                this.excludeArtifactIds ) );

        // start with all artifacts.
        Set< Artifact > artifacts = project.getArtifacts();

        // perform filtering
        try
        {
            artifacts = filter.filter( artifacts );

            Iterator< Artifact > iterator = artifacts.iterator();
            while ( iterator.hasNext() )
            {
                Artifact artifact = iterator.next();
                if ( !scope.equalsIgnoreCase( artifact.getScope() ) )
                {
                    iterator.remove();
                }
            }
        }
        catch ( ArtifactFilterException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }

        if ( artifacts.isEmpty() )
        {
            getLog().warn( "No RSL dependency found" );
        }
        else
        {
            outputDirectory.mkdirs();
            for ( Artifact artifact : artifacts )
            {
                Artifact rslArtifact = artifactFactory
                        .createArtifactWithClassifier( artifact.getGroupId(),
                                artifact.getArtifactId(),
                                artifact.getVersion(), rslExtension, null );

                processDependency( artifact, rslArtifact );
            }
        }
    }

    /**
     * @param artifact
     * @throws ArtifactResolutionException
     * @throws ArtifactNotFoundException
     * @throws IOException
     * @throws MojoExecutionException
     */
    protected void processDependency(Artifact artifact, Artifact rslArtifact)
            throws MojoExecutionException
    {
        try
        {
            // lookup RSL artifact
            resolver.resolve( rslArtifact, remoteRepositories, localRepository );
            getLog().debug( "Artifact RSL found: " + rslArtifact );
            File outputFile = new File( outputDirectory,
                    getFormattedFileName( rslArtifact ) );
            FileUtils.copyFile( rslArtifact.getFile(), outputFile );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }

    protected String getFormattedFileName(Artifact artifact)
    {
        String destFileName = null;
        if ( artifact.getFile() != null && !stripVersion )
        {
            destFileName = artifact.getFile().getName();
        }
        else
        {
            String versionString = null;
            if ( !stripVersion )
            {
                versionString = "-" + artifact.getVersion();
            }
            else
            {
                versionString = "";
            }

            String classifierString = "";

            if ( artifact.getClassifier() != null
                    && !artifact.getClassifier().trim().isEmpty() )
            {
                classifierString = "-" + artifact.getClassifier();
            }

            destFileName = artifact.getArtifactId() + versionString
                    + classifierString + "."
                    + artifact.getArtifactHandler().getExtension();
        }
        return destFileName;
    }
}
