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
package org.sonatype.flexmojos.sandbox.bundlepublisher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.DefaultArtifact;
import org.apache.maven.mercury.plexus.PlexusMercury;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.Organization;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.flexmojos.sandbox.bundlepublisher.model.Artifact;
import org.sonatype.flexmojos.sandbox.bundlepublisher.model.ArtifactDependency;
import org.sonatype.flexmojos.sandbox.bundlepublisher.model.BundleDescriptor;

@Component( role = BundlePublisher.class )
public class DefaultBundlePublisher
    extends AbstractLogEnabled
    implements BundlePublisher
{

    private static final File TEMP_DIR = new File( System.getProperty( "java.io.tmpdir" ) );

    private static final Random RANDOM = new Random();

    @Requirement
    private PlexusMercury mercury;

    private final List<File> temporaryFiles = new ArrayList<File>();

    public void publish( File sourceFile, InputStream bundleDescriptor, Repository repository )
        throws PublishingException, RepositoryException
    {
        validate( sourceFile, bundleDescriptor );

        BundleDescriptor descriptor;
        try
        {
            descriptor = BundleDescriptor.read( bundleDescriptor );
        }
        catch ( Exception e )
        {
            throw new PublishingException( "Unable to parse descriptor file", e );
        }

        Collection<org.apache.maven.mercury.artifact.Artifact> artifacts =
            new ArrayList<org.apache.maven.mercury.artifact.Artifact>();

        ZipFile zip = null;
        try
        {
            zip = new ZipFile( sourceFile );
            validate( descriptor, zip );

            for ( Artifact artifact : descriptor.getArtifacts() )
            {
                getLogger().debug( "Importing artifact " + artifact.getArtifactId() );

                Model pom = createMavenModel( descriptor, artifact );
                DefaultArtifact mercuryArtifact = createMercuryArtifact( descriptor, artifact );
                mercuryArtifact.setPomBlob( toBlob( pom ) );

                String location = artifact.getLocation();
                File file;
                if ( location != null )
                {
                    file = getArtifactFile( zip, location, mercuryArtifact );
                }
                else
                {
                    file = toFile( pom );
                }

                mercuryArtifact.setFile( file );

                artifacts.add( mercuryArtifact );
            }
        }
        catch ( IOException e )
        {
            throw new PublishingException( "Unable to open souce file", e );
        }
        finally
        {
            if ( zip != null )
            {
                try
                {
                    zip.close();
                }
                catch ( IOException e )
                {
                    // just closing
                }
            }
        }

        this.mercury.write( repository, artifacts );

        killTemporaryFiles();
    }

    private File toFile( Model pom )
        throws IOException
    {
        FileWriter writer = null;
        try
        {
            File file = createTempFile( pom.getArtifactId(), pom.getPackaging() );
            file.createNewFile();

            writer = new FileWriter( file );
            new MavenXpp3Writer().write( writer, pom );

            return file;
        }
        finally
        {
            IOUtil.close( writer );
        }
    }

    private byte[] toBlob( Model pom )
        throws PublishingException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter( output );
        try
        {
            new MavenXpp3Writer().write( writer, pom );
        }
        catch ( IOException e )
        {
            throw new PublishingException( "Unable to convert pom into a byte array", e );
        }
        IOUtil.close( writer );
        IOUtil.close( output );

        return output.toByteArray();
    }

    private File getArtifactFile( ZipFile zip, String location, DefaultArtifact mercuryArtifact )
        throws IOException
    {
        ZipEntry entry = zip.getEntry( location );
        InputStream input = null;
        FileOutputStream output = null;
        try
        {
            File file = createTempFile( FileUtils.removeExtension( location ), FileUtils.getExtension( location ) );
            file.createNewFile();

            input = zip.getInputStream( entry );
            output = new FileOutputStream( file );
            IOUtil.copy( input, output );
            output.flush();

            return file;
        }
        finally
        {
            IOUtil.close( input );
            IOUtil.close( output );
        }
    }

    private void killTemporaryFiles()
    {
        for ( File tempFile : temporaryFiles )
        {
            try
            {
                FileUtils.forceDelete( tempFile );
            }
            catch ( IOException e )
            {
                // just cleaning
                getLogger().warn( "Unable to delete temporary file " + tempFile.getAbsolutePath(), e );
            }
        }
    }

    private DefaultArtifact createMercuryArtifact( BundleDescriptor descriptor, Artifact artifact )
    {
        String groupId = descriptor.getDefaults().getGroupId();
        String artifactId = artifact.getArtifactId();
        String version = descriptor.getDefaults().getVersion();
        String type = artifact.getType() != null ? artifact.getType() : "jar";

        DefaultArtifact mercuryArtifact =
            new DefaultArtifact( groupId, artifactId, version, type, null, false, null, null );

        mercuryArtifact.setDependencies( new ArrayList<ArtifactBasicMetadata>() );
        for ( ArtifactDependency dependency : artifact.getDependencies() )
        {
            String depGroupId = dependency.getGroupId() != null ? dependency.getGroupId() : groupId;
            String depVersion = dependency.getVersion() != null ? dependency.getVersion() : version;
            String depType = dependency.getType() != null ? dependency.getType() : "jar";

            ArtifactBasicMetadata dep = new ArtifactBasicMetadata();
            dep.setGroupId( depGroupId );
            dep.setArtifactId( dependency.getArtifactId() );
            dep.setClassifier( dependency.getClassifier() );
            dep.setType( depType );
            dep.setVersion( depVersion );

            mercuryArtifact.getDependencies().add( dep );
        }
        return mercuryArtifact;
    }

    private Model createMavenModel( BundleDescriptor descriptor, Artifact artifact )
        throws PublishingException
    {
        String groupId = descriptor.getDefaults().getGroupId();
        String artifactId = artifact.getArtifactId();
        String version = descriptor.getDefaults().getVersion();
        String type = artifact.getType() != null ? artifact.getType() : "jar";

        Model pom = new Model();
        pom.setGroupId( groupId );
        pom.setArtifactId( artifactId );
        pom.setVersion( version );
        pom.setPackaging( type );
        if ( descriptor.getOrganization() != null )
        {
            Organization organization = new Organization();
            organization.setName( descriptor.getOrganization().getName() );
            organization.setUrl( descriptor.getOrganization().getUrl() );
            pom.setOrganization( organization );

            String licenseText = descriptor.getOrganization().getLicense();
            if ( licenseText != null )
            {
                License license = new License();
                license.setComments( licenseText );
                pom.addLicense( license );
            }
        }

        for ( ArtifactDependency dependency : artifact.getDependencies() )
        {
            String depGroupId = dependency.getGroupId() != null ? dependency.getGroupId() : groupId;
            String depVersion = dependency.getVersion() != null ? dependency.getVersion() : version;
            String depType = dependency.getType() != null ? dependency.getType() : "jar";

            Dependency dep = new Dependency();
            dep.setGroupId( depGroupId );
            dep.setArtifactId( dependency.getArtifactId() );
            dep.setClassifier( dependency.getClassifier() );
            dep.setType( depType );
            dep.setVersion( depVersion );
            pom.addDependency( dep );
        }

        return pom;
    }

    private void validate( BundleDescriptor descriptor, ZipFile zip )
        throws PublishingException
    {
        getLogger().debug( "Validating descriptor" );

        if ( descriptor.getDefaults() == null )
        {
            throw new PublishingException( "Invalid descriptor: Defaults is not defined!" );
        }
        if ( descriptor.getDefaults().getGroupId() == null )
        {
            throw new PublishingException( "Invalid descriptor: Default groupId is not defined!" );
        }
        if ( descriptor.getDefaults().getVersion() == null )
        {
            throw new PublishingException( "Invalid descriptor: Default version is not defined!" );
        }

        if ( descriptor.getArtifacts().isEmpty() )
        {
            throw new PublishingException( "Invalid descriptor: No artifacts defined!" );
        }

        for ( Artifact artifact : descriptor.getArtifacts() )
        {
            if ( artifact.getArtifactId() == null )
            {
                throw new PublishingException( "Invalid descriptor: Artifact ID not defined!" );
            }
            if ( artifact.getLocation() == null )
            {
                if ( !"pom".equals( artifact.getType() ) )
                {
                    throw new PublishingException( "Invalid descriptor: Artifact location not defined!" );
                }

            }
            else
            {
                ZipEntry entry = zip.getEntry( artifact.getLocation() );
                if ( entry == null )
                {
                    throw new PublishingException( "Artifact not found on sourceFile: " + artifact.getLocation() );
                }
            }

        }
    }

    private void validate( File sourceFile, InputStream bundleDescriptor )
        throws PublishingException
    {
        getLogger().debug( "Validating inputs" );
        if ( sourceFile == null )
        {
            throw new PublishingException( "Source file not defined. Please define a valid source file!" );
        }

        if ( !sourceFile.exists() )
        {
            throw new PublishingException( "Unable to find source file: " + sourceFile.getAbsolutePath() );
        }

        if ( bundleDescriptor == null )
        {
            throw new PublishingException( "Bundle descriptor not defined. Please define a valid bundle descriptor!" );
        }
    }

    private File createTempFile( String prefix, String suffix )
    {
        File tempFile = new File( TEMP_DIR, prefix + "-" + Long.toHexString( RANDOM.nextInt( 4 ) ) + "-" + suffix );
        tempFile.getParentFile().mkdirs();

        temporaryFiles.add( tempFile );
        return tempFile;
    }

}
