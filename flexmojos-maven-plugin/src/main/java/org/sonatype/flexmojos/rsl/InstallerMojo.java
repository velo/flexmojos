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
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.flexmojos.MavenMojo;

import flex2.compiler.io.FileUtil;

/**
 * Goal that allows for manual generation of RSLs. This goal is intended to be
 * run out of the build lifecycle to install or deploy SWF files for SWC
 * artifacts missing their RSL counterpart.
 * 
 * Tipical usage examples are:
 * 
 * <ul>
 * <li>to generate and install (or deploy) a specific artifact RSL</br>
 * <code>mvn flexmojos:install-rsl -DartifactId= -DgroupId= -Dversion= -Dclassifier=</code>
 * </li>
 * 
 * <li>to generate and install (or deploy) all direct dependencies RSLs</br>
 * <code>mvn flexmojos:install-rsl -Ddependencies=direct</code></li>
 * 
 * <li>to generate and install (or deploy) all transitive dependencies RSLs</br>
 * <code>mvn flexmojos:install-rsl -Ddependencies=transitive</code></li>
 * 
 * @author Roberto Lo Giacco (rlogiacco@gmail.com)
 * 
 * @goal install-rsl
 * @requiresDependencyResolution
 */
public class InstallerMojo extends DependencyProcessorMojo implements MavenMojo
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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if ( dependencies == null )
        {
            Artifact artifact = artifactFactory.createArtifactWithClassifier(
                    groupId, artifactId, version, originalType, classifier );

            Artifact rslArtifact = artifactFactory
                    .createArtifactWithClassifier( groupId, artifactId,
                            version, rslExtension, classifier );

            try
            {
                resolver.resolve( artifact, remoteRepositories, localRepository );
                try
                {
                    resolver.resolve( rslArtifact, remoteRepositories,
                            localRepository );
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
                throw new MojoExecutionException( "Artifact resolution failed",
                        e );
            }
        }
        else if ( "direct".equalsIgnoreCase( dependencies ) )
        {
            excludeTransitive = true;
            super.execute();
        }
        else if ( "transitive".equalsIgnoreCase( dependencies ) )
        {
            super.execute();
        }
        else
        {
            throw new MojoExecutionException(
                    "No valid execution parameters found" );
        }
    }

    @Override
    protected void processDependency(Artifact artifact, Artifact rslArtifact)
            throws MojoExecutionException
    {

        try
        {
            super.processDependency( artifact, rslArtifact );
        }
        catch ( MojoExecutionException mee )
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
                File outputFile = new File( outputDirectory,
                        getFormattedFileName( rslArtifact ) );
                output = new FileOutputStream( outputFile );
                Artifact originalArtifact = artifactFactory
                        .createArtifactWithClassifier( artifact.getGroupId(),
                                artifact.getArtifactId(),
                                artifact.getVersion(), artifact.getType(),
                                originalClassifier );

                if ( optimizeRsls )
                {
                    originalFile = new File( project.getBuild()
                            .getOutputDirectory(), artifact.getFile().getName()
                            + originalClassifier );
                    FileUtils.copyFile( artifact.getFile(), originalFile );
                    getLog().info( "Attempting to optimize: " + artifact );
                    long initialSize = artifact.getFile().length() / 1024;
                    optimize( input, output );
                    long optimizedSize = outputFile.length() / 1024;
                    getLog().info(
                            "\t\tsize reduced from " + initialSize + "kB to "
                                    + optimizedSize + "kB" );

                    updateDigest( outputFile, originalFile );
                }
                if ( deploy )
                {
                    ArtifactRepository deploymentRepository = project
                            .getDistributionManagementArtifactRepository();
                    if ( backup && optimizeRsls )
                    {
                        deployer.deploy( originalFile, originalArtifact,
                                deploymentRepository, localRepository );
                    }
                    deployer.deploy( outputFile, rslArtifact,
                            deploymentRepository, localRepository );
                }
                else
                {
                    if ( backup && optimizeRsls )
                    {
                        installer.install( originalFile, originalArtifact,
                                localRepository );
                    }
                    installer
                            .install( outputFile, rslArtifact, localRepository );
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
}
