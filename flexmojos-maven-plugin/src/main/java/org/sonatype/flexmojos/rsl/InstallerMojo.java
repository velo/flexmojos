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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
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
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.flexmojos.MavenMojo;
import org.sonatype.flexmojos.optimizer.OptimizerMojo;

/**
 * Goal that allows for manual generation of RSLs. This goal is intended to be run out of the build lifecycle to install
 * or deploy SWF files for SWC artifacts missing their RSL counterpart. Tipical usage examples are:
 * <ul>
 * <li>to generate and install (or deploy) a specific artifact RSL</br>
 * <code>mvn flexmojos:install-rsl -DartifactId= -DgroupId= -Dversion= -Dclassifier=</code></li>
 * <li>to generate and install (or deploy) all direct dependencies RSLs</br>
 * <code>mvn flexmojos:install-rsl -Ddependencies=direct</code></li>
 * <li>to generate and install (or deploy) all transitive dependencies RSLs</br>
 * <code>mvn flexmojos:install-rsl -Ddependencies=transitive</code></li>
 * 
 * @author Roberto Lo Giacco (rlogiacco@gmail.com)
 * @goal install-rsl
 * @requiresDependencyResolution
 * @requiresDirectInvocation true
 * @requiresProject false
 */
public class InstallerMojo
    extends OptimizerMojo
    implements MavenMojo
{

    /**
     * @optional
     * @parameter expression="${deploy}" default-value="false"
     */
    private boolean deploy;

    /**
     * @optional
     * @parameter expression="${backup}" default-value="true"
     */
    private boolean backup;

    /**
     * @parameter expression="${originalType}" default-value="swc"
     */
    private String originalType;

    /**
     * @parameter expression="${originalClassifier}" default-value="original"
     */
    private String originalClassifier;

    /**
     * @optional
     * @parameter expression="${dependencies}"
     */
    private String dependencies;

    /**
     * @parameter expression="${artifactId}"
     */
    private String artifactId;

    /**
     * @parameter expression="${groupId}"
     */
    private String groupId;

    /**
     * @parameter expression="${version}"
     */
    private String version;

    /**
     * @optional
     * @parameter expression="${classifier}"
     */
    private String classifier;

    /**
     * @component
     * @readonly
     * @required
     */
    private ArtifactDeployer deployer;

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
     */
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
    protected List<ArtifactRepository> remoteRepositories;

    /**
     * @component
     * @readonly
     * @required
     */
    protected ArtifactInstaller installer;

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( dependencies == null )
        {
            Artifact artifact =
                artifactFactory.createArtifactWithClassifier( groupId, artifactId, version, originalType, classifier );

            Artifact rslArtifact =
                artifactFactory.createArtifactWithClassifier( groupId, artifactId, version, rslExtension, classifier );

            try
            {
                resolver.resolve( artifact, remoteRepositories, localRepository );
                try
                {
                    resolver.resolve( rslArtifact, remoteRepositories, localRepository );
                    getLog().info( "Overwriting RSL Artifact" );
                }
                catch ( Exception e )
                {
                    getLog().debug( "Generating the RSL Artifact" );
                }
                processDependency( artifact, rslArtifact );
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( "Artifact resolution failed", e );
            }
        }
        else if ( "direct".equalsIgnoreCase( dependencies ) )
        {
            excludeTransitive = true;
            processDependencies();
        }
        else if ( "transitive".equalsIgnoreCase( dependencies ) )
        {
            processDependencies();
        }
        else
        {
            throw new MojoExecutionException( "No valid execution parameters found" );
        }
    }

    /**
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    @SuppressWarnings( "unchecked" )
    protected void processDependencies()
        throws MojoExecutionException, MojoFailureException
    {
        // add filters in well known order, least specific to most specific
        FilterArtifacts filter = new FilterArtifacts();
        filter.addFilter( new ProjectTransitivityFilter( project.getDependencyArtifacts(), this.excludeTransitive ) );
        filter.addFilter( new TypeFilter( this.includeTypes, this.excludeTypes ) );
        filter.addFilter( new ClassifierFilter( this.includeClassifiers, this.excludeClassifiers ) );
        filter.addFilter( new GroupIdFilter( this.includeGroupIds, this.excludeGroupIds ) );
        filter.addFilter( new ArtifactIdFilter( this.includeArtifactIds, this.excludeArtifactIds ) );

        // start with all artifacts.
        Set<Artifact> artifacts = project.getArtifacts();

        // perform filtering
        try
        {
            artifacts = filter.filter( artifacts );

            Iterator<Artifact> iterator = artifacts.iterator();
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
                Artifact rslArtifact =
                    artifactFactory.createArtifactWithClassifier( artifact.getGroupId(), artifact.getArtifactId(),
                                                                  artifact.getVersion(), rslExtension, null );

                processDependency( artifact, rslArtifact );
            }
        }
    }

    /**
     * @param artifact
     * @param rslArtifact
     * @throws MojoExecutionException
     */
    protected void processDependency( Artifact artifact, Artifact rslArtifact )
        throws MojoExecutionException
    {

        try
        {
            // lookup RSL artifact
            resolver.resolve( rslArtifact, remoteRepositories, localRepository );
            getLog().debug( "Artifact RSL found: " + rslArtifact );
            File outputFile = new File( outputDirectory, getFormattedFileName( rslArtifact ) );
            try
            {
                FileUtils.copyFile( rslArtifact.getFile(), outputFile );
            }
            catch ( IOException ioe )
            {
                throw new MojoExecutionException( ioe.getMessage(), ioe );
            }
        }
        catch ( AbstractArtifactResolutionException aare )
        {
            // RSL not available then create it
            ZipFile archive = null;
            InputStream input = null;
            OutputStream output = null;
            File originalFile = null;
            try
            {
                resolver.resolve( artifact, remoteRepositories, localRepository );
                archive = newZipFile( artifact.getFile() );
                input = readLibrarySwf( artifact.getFile(), archive );
                File outputFile = new File( outputDirectory, getFormattedFileName( rslArtifact ) );
                output = new FileOutputStream( outputFile );
                Artifact originalArtifact =
                    artifactFactory.createArtifactWithClassifier( artifact.getGroupId(), artifact.getArtifactId(),
                                                                  artifact.getVersion(), artifact.getType(),
                                                                  originalClassifier );

                if ( optimizeRsls )
                {
                    originalFile =
                        new File( project.getBuild().getOutputDirectory(), artifact.getFile().getName()
                            + originalClassifier );
                    FileUtils.copyFile( artifact.getFile(), originalFile );
                    getLog().info( "Attempting to optimize: " + artifact );
                    long initialSize = artifact.getFile().length() / 1024;
                    optimize( input, output );
                    long optimizedSize = outputFile.length() / 1024;
                    getLog().info( "\t\tsize reduced from " + initialSize + "kB to " + optimizedSize + "kB" );

                    updateDigest( outputFile, originalFile );
                }
                if ( deploy )
                {
                    ArtifactRepository deploymentRepository = project.getDistributionManagementArtifactRepository();
                    if ( backup && optimizeRsls )
                    {
                        deployer.deploy( originalFile, originalArtifact, deploymentRepository, localRepository );
                    }
                    deployer.deploy( outputFile, rslArtifact, deploymentRepository, localRepository );
                }
                else
                {
                    if ( backup && optimizeRsls )
                    {
                        installer.install( originalFile, originalArtifact, localRepository );
                    }
                    installer.install( outputFile, rslArtifact, localRepository );
                }
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
            finally
            {
                IOUtil.close( input );
                IOUtil.close( output );
                if ( archive != null )
                {
                    try
                    {
                        archive.close();
                        if ( originalFile != null )
                        {
                            originalFile.delete();
                        }
                    }
                    catch ( IOException e )
                    {
                        // ignore
                    }
                }
            }
        }
    }

    /**
     * @param artifact
     * @return
     */
    protected String getFormattedFileName( Artifact artifact )
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

            if ( artifact.getClassifier() != null && !artifact.getClassifier().trim().isEmpty() )
            {
                classifierString = "-" + artifact.getClassifier();
            }

            destFileName =
                artifact.getArtifactId() + versionString + classifierString + "."
                    + artifact.getArtifactHandler().getExtension();
        }
        return destFileName;
    }
}
