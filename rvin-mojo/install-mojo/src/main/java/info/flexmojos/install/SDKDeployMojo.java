package info.flexmojos.install;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.repository.ArtifactRepository;

public class SDKDeployMojo
    extends AbstractInstallMojo
{

    /**
     * @component
     */
    private ArtifactDeployer deployer;

    /**
     * @parameter expression="${localRepository}"
     */
    private ArtifactRepository localRepository;

    /**
     * @parameter expression="${project.distributionManagementArtifactRepository}"
     */
    private ArtifactRepository deploymentRepository;

    @Override
    public void installArtifact( File file, Artifact artifact )
    {
        try
        {
            deployer.deploy( file, artifact, deploymentRepository, localRepository );
        }
        catch ( ArtifactDeploymentException e )
        {
            getLog().error( "Unable to install artifact: " + file.getAbsolutePath(), e );
        }
    }

}
