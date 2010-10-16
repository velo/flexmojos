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
package org.sonatype.flexmojos.plugin.unused;

import static org.sonatype.flexmojos.plugin.common.FlexClassifier.LINK_REPORT;
import static org.sonatype.flexmojos.util.PathUtil.files;
import static org.sonatype.flexmojos.util.PathUtil.filesList;
import static org.sonatype.flexmojos.util.PathUtil.path;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.PatternSet;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.repository.legacy.metadata.ArtifactMetadata;
import org.codehaus.plexus.util.DirectoryScanner;
import org.sonatype.flexmojos.plugin.AbstractMavenMojo;
import org.sonatype.flexmojos.plugin.SourcePathAware;
import org.sonatype.flexmojos.plugin.compiler.metadata.ReportMetadata;
import org.sonatype.flexmojos.util.LinkReportUtil;
import org.sonatype.flexmojos.util.PathUtil;

/**
 * This goal checks if all source files are included on this build
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @goal unused-check
 * @phase verify
 * @since 4.0
 */
public class UnusedSourceMojo
    extends AbstractMavenMojo
    implements SourcePathAware
{

    /**
     * The maven compile source roots
     * <p>
     * Equivalent to -compiler.source-path
     * </p>
     * List of path elements that form the roots of ActionScript class
     * 
     * @parameter expression="${project.compileSourceRoots}"
     * @required
     * @readonly
     */
    private List<String> compileSourceRoots;

    /**
     * When true Flexmojos will compare the content of Maven source roots with the classes listed on link reports. If
     * any file is on source roots isn't used it will fail the build.
     * 
     * @parameter expression="${flex.failIfUnused}"
     */
    private boolean failIfUnused;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        List<File> unusedFiles = getUnusedFiles();

        if ( unusedFiles.isEmpty() )
        {
            getLog().info( "All files included" );
            // nice, all files included ;)
            return;
        }

        getLog().error( "Some files were not included on the build:" );
        for ( File file : unusedFiles )
        {
            getLog().error( path( file ) );
        }

        if ( failIfUnused )
        {
            throw new MojoExecutionException( "Some files were not included on the build, check logs" );
        }
    }

    public File[] getSourcePath()
    {
        return PathUtil.existingFiles( compileSourceRoots );
    }

    protected List<File> getUnusedFiles()
    {
        List<Artifact> artifacts = new ArrayList<Artifact>();
        artifacts.add( project.getArtifact() );
        artifacts.addAll( project.getAttachedArtifacts() );

        List<ReportMetadata> linkReportMetadatas = new ArrayList<ReportMetadata>();
        for ( Artifact artifact : artifacts )
        {
            @SuppressWarnings( "deprecation" )
            Collection<org.apache.maven.artifact.metadata.ArtifactMetadata> metadatas = artifact.getMetadataList();
            for ( ArtifactMetadata metadata : metadatas )
            {
                if ( metadata instanceof ReportMetadata )
                {
                    ReportMetadata reportMetata = (ReportMetadata) metadata;
                    if ( LINK_REPORT.equals( reportMetata.getClassifier() ) )
                    {
                        linkReportMetadatas.add( reportMetata );
                    }
                }
            }
        }

        List<File> includedFiles = new ArrayList<File>();
        for ( ReportMetadata reportMetadata : linkReportMetadatas )
        {
            includedFiles.addAll( filesList( LinkReportUtil.getLinkedFiles( reportMetadata.getFile() ) ) );
        }

        List<File> availableFile = new ArrayList<File>();
        for ( File sourceDir : getSourcePath() )
        {
            DirectoryScanner scan = scan( new PatternSet(), sourceDir );
            availableFile.addAll( files( scan.getIncludedFiles(), scan.getBasedir() ) );
        }

        availableFile.removeAll( includedFiles );
        return availableFile;
    }

}
