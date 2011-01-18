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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
 * @goal install-rsl
 * @phase package
 */
public class InstallerMojo extends OptimizerMojo implements MavenMojo
{
    /**
     * @parameter default-value="swf"
     */
    private String rslExtension;

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        File originalFile = project.getArtifact().getFile();
        ZipFile archive = newZipFile( originalFile );
        InputStream input = readLibrarySwf( originalFile, archive );
        String noExtensionFilename = originalFile.getName().substring( 0,
                originalFile.getName().lastIndexOf( '.' ) );
        File outputFile = new File( project.getBuild().getDirectory(),
                noExtensionFilename + '.' + rslExtension );

        try
        {
            FileOutputStream output = new FileOutputStream( outputFile );
            byte[] buffer = new byte[ 1024 ];
            for ( int read = input.read( buffer ); read > 0; read = input
                    .read( buffer ) )
            {
                output.write( buffer, 0, read );
            }
            output.close();
            input.close();
            archive.close();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }

        projectHelper.attachArtifact( project, rslExtension, outputFile );
    }
}
