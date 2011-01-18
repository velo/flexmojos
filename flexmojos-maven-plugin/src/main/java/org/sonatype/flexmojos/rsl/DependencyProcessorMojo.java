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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
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
 * @author rlogiacco
 * 
 * @goal process-rsl
 * @phase process-resources
 */
public class DependencyProcessorMojo extends OptimizerMojo implements
        MavenMojo
{
    /**
     * @optional
     * @parameter expression="${excludeTransitive}" default-value="false"
     */
    private boolean excludeTransitive;

    /**
     * @parameter expression="${project.build.directory}/rsl"
     */
    private File outputDirectory;

    /**
     * @parameter default-value="rsl"
     */
    private String scope;

    /**
     * @parameter default-value="swf"
     */
    private String rslExtension;

    /**
     * @parameter default-value="original"
     */
    private String originalClassifier;

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
     * @optional
     * @parameter default-value="false"
     */
    private boolean deploy;

    /**
     * @optional
     * @parameter default-value="true"
     */
    private boolean backup;

    /**
     * @component
     * @readonly
     * @required
     */
    private ArtifactFactory artifactFactory;

    /**
     * @component
     * @readonly
     * @required
     * */
    private ArtifactResolver resolver;

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
    private ArtifactInstaller installer;

    /**
     * @component
     * @readonly
     * @required
     */
    private ArtifactDeployer deployer;

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

        // start with rsl artifacts.
        Set< Artifact > artifacts = new HashSet< Artifact >();

        List< Dependency > dependencies = project.getDependencies();
        for ( Dependency dependency : dependencies )
        {
            try
            {
                if ( scope.equalsIgnoreCase( dependency.getScope() ) )
                {
                    VersionRange versionRange = VersionRange
                            .createFromVersionSpec( dependency.getVersion() );
                    Artifact artifact = artifactFactory
                            .createDependencyArtifact( dependency.getGroupId(),
                                    dependency.getArtifactId(), versionRange,
                                    dependency.getType(),
                                    dependency.getClassifier(), scope );
                    List< ArtifactVersion > versions = artifactMetadataSource
                            .retrieveAvailableVersions( artifact,
                                    localRepository, remoteRepositories );
                    artifacts.add( artifact );
                    artifact.setVersion( versionRange.matchVersion( versions )
                            .toString() );
                }
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
        }
        // perform filtering
        try
        {
            artifacts = filter.filter( artifacts );
        }
        catch ( ArtifactFilterException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }

        if ( !artifacts.isEmpty() )
        {
            outputDirectory.mkdirs();
        }

        for ( Artifact artifact : artifacts )
        {
            try
            {
                Artifact rslArtifact = artifactFactory
                        .createArtifactWithClassifier( artifact.getGroupId(),
                                artifact.getArtifactId(),
                                artifact.getVersion(), rslExtension, null );

                try
                {
                    // if RSL is already available then use it
                    resolver.resolve( rslArtifact, remoteRepositories,
                            localRepository );
                    getLog().debug(
                            "Artifact RSL already available: " + rslArtifact );
                    File outputFile = new File( outputDirectory, rslArtifact
                            .getFile().getName() );
                    FileUtils.copyFile( rslArtifact.getFile(), outputFile );
                }
                catch ( AbstractArtifactResolutionException aare )
                {
                    // RSL not available then create it
                    resolver.resolve( artifact, remoteRepositories,
                            localRepository );

                    getLog().info( "Attempting to optimize: " + artifact );
                    File originalFile = artifact.getFile();

                    ZipFile archive = newZipFile( originalFile );
                    InputStream input = readLibrarySwf( originalFile, archive );
                    String noExtensionFilename = originalFile.getName()
                            .substring( 0,
                                    originalFile.getName().lastIndexOf( '.' ) );
                    File outputFile = new File( outputDirectory,
                            noExtensionFilename + '.' + rslExtension );
                    FileOutputStream output = new FileOutputStream( outputFile );
                    long initialSize = originalFile.length() / 1024;
                    optimize( input, output );
                    long optimizedSize = outputFile.length() / 1024;
                    getLog().info(
                            "\t\tsize reduced from " + initialSize + "kB to "
                                    + optimizedSize + "kB" );
                    if ( backup )
                    {
                        Artifact originalArtifact = artifactFactory
                                .createArtifactWithClassifier(
                                        artifact.getGroupId(),
                                        artifact.getArtifactId(),
                                        artifact.getVersion(),
                                        artifact.getType(), originalClassifier );
                        installer.install( originalFile, originalArtifact,
                                localRepository );
                    }
                    updateDigest( outputFile, originalFile );
                    if ( deploy )
                    {
                        ArtifactRepository deploymentRepository = project
                                .getDistributionManagementArtifactRepository();
                        deployer.deploy( outputFile, rslArtifact,
                                deploymentRepository, localRepository );
                    }
                    else
                    {
                        installer.install( outputFile, rslArtifact,
                                localRepository );
                    }
                    output.close();
                    input.close();
                    archive.close();

                }
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
        }
    }
}
