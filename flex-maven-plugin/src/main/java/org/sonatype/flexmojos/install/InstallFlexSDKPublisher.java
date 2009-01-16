package org.sonatype.flexmojos.install;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.flexmojos.components.publisher.AbstractFlexSDKPublisher;
import org.sonatype.flexmojos.components.publisher.FlexSDKPublisher;
import org.sonatype.flexmojos.components.publisher.PublishingException;

@Component( role = FlexSDKPublisher.class, hint = "install" )
public class InstallFlexSDKPublisher
    extends AbstractFlexSDKPublisher
    implements FlexSDKPublisher
{
    @Requirement
    private ArtifactInstaller installer;

    @Override
    protected void publishArtifact( Artifact artifact )
        throws PublishingException
    {
        File file = artifact.getFile();
        try
        {
            ArtifactRepository localRepository = (ArtifactRepository) context.get( "localRepository" );
            installer.install( file, artifact, localRepository ); // to install
        }
        catch ( Exception e )
        {
            getLogger().error( "Unable to install artifact: " + file.getAbsolutePath(), e );
            throw new PublishingException( "Unable to install artifact: " + file.getAbsolutePath(), e );
        }

    }

}
