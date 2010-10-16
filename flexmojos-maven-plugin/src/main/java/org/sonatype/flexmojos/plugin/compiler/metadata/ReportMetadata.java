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
package org.sonatype.flexmojos.plugin.compiler.metadata;

import java.io.File;
import java.io.IOException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.AbstractArtifactMetadata;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataStoreException;
import org.codehaus.plexus.util.FileUtils;

@SuppressWarnings( "deprecation" )
public class ReportMetadata
    extends AbstractArtifactMetadata
{

    private boolean attach;

    private String classifier;

    private File file;

    private String type;

    public ReportMetadata( Artifact artifact, File file, boolean attach, String classifier, String type )
    {
        super( artifact );
        this.file = file;
        this.attach = attach;
        this.classifier = classifier;
        this.type = type;
    }

    public String getBaseVersion()
    {
        return artifact.getBaseVersion();
    }

    public String getClassifier()
    {
        return classifier;
    }

    public File getFile()
    {
        return file;
    }

    private String getFilename()
    {
        StringBuffer buf = new StringBuffer( 128 );
        buf.append( getArtifactId() );
        buf.append( "-" ).append( artifact.getVersion() );
        buf.append( "-" ).append( classifier );
        buf.append( "." ).append( type );
        return buf.toString();
    }

    public Object getKey()
    {
        return classifier + " " + artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getType()
            + ":" + classifier + ":" + type;
    }

    public String getLocalFilename( ArtifactRepository repository )
    {
        return getFilename();
    }

    public String getRemoteFilename()
    {
        return getFilename();
    }

    public void merge( ArtifactMetadata metadata )
    {
        merge( (org.apache.maven.repository.legacy.metadata.ArtifactMetadata) metadata );
    }

    public void merge( org.apache.maven.repository.legacy.metadata.ArtifactMetadata metadata )
    {
        ReportMetadata m = (ReportMetadata) metadata;
        if ( !m.file.equals( file ) )
        {
            throw new IllegalStateException( "Cannot add two different pieces of metadata for: " + getKey() );
        }
    }

    public boolean storedInArtifactVersionDirectory()
    {
        return attach;
    }

    public void storeInLocalRepository( ArtifactRepository localRepository, ArtifactRepository remoteRepository )
        throws RepositoryMetadataStoreException
    {
        File destination =
            new File( localRepository.getBasedir(), localRepository.pathOfLocalRepositoryMetadata( this,
                                                                                                   remoteRepository ) );

        try
        {
            FileUtils.copyFile( file, destination );
        }
        catch ( IOException e )
        {
            throw new RepositoryMetadataStoreException( "Error copying " + classifier + " to the local repository.", e );
        }
    }

    public String toString()
    {
        return getFilename();
    }

}
