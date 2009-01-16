package org.sonatype.flexmojos.install.publisher;

import java.io.File;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.flexmojos.components.publisher.AbstractFlexSDKPublisher;
import org.sonatype.flexmojos.components.publisher.FlexSDKPublisher;
import org.sonatype.flexmojos.components.publisher.PublishingException;

@Component( role = FlexSDKPublisher.class, hint = "deploy" )
public class DeployFlexSDKPublisher
    extends AbstractFlexSDKPublisher
    implements FlexSDKPublisher
{
    @Requirement
    private Map<String, ArtifactRepositoryLayout> repositoryLayouts;

    @Requirement
    private ArtifactRepositoryFactory repositoryFactory;

    @Requirement
    private ArtifactDeployer deployer;

    @Override
    protected void publishArtifact( Artifact artifact )
        throws PublishingException
    {
        File file = artifact.getFile();

        try
        {
            String repositoryLayout = (String) context.get( "repositoryLayout" );
            String repositoryId = (String) context.get( "repositoryId" );
            String url = (String) context.get( "url" );
            boolean uniqueVersion = (Boolean) context.get( "uniqueVersion" );
            ArtifactRepository localRepository = (ArtifactRepository) context.get( "localRepository" );

            ArtifactRepositoryLayout layout = repositoryLayouts.get( repositoryLayout );

            ArtifactRepository deploymentRepository =
                repositoryFactory.createDeploymentArtifactRepository( repositoryId, url, layout, uniqueVersion );

            deployer.deploy( file, artifact, deploymentRepository, localRepository );
        }
        catch ( Exception e )
        {
            getLogger().error( "Unable to deploy artifact: " + file.getAbsolutePath(), e );
            throw new PublishingException( "Unable to deploy artifact: " + file.getAbsolutePath(), e );
        }

    }

}
