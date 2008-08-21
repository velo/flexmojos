package info.flexmojos.install;

import java.io.File;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;

/**
 * @goal deploy-sdk
 * @requiresProject false
 * @requiresDirectInvocation true
 * @author marvin
 */
public class SDKDeployMojo
    extends AbstractInstallMojo
{

    /**
     * Server Id to map on the &lt;id&gt; under &lt;server&gt; section of settings.xml
     * In most cases, this parameter will be required for authentication.
     *
     * @parameter expression="${repositoryId}" default-value="remote-repository"
     * @required
     */
    private String repositoryId;

    /**
     * The type of remote repository layout to deploy to. Try <i>legacy</i> for 
     * a Maven 1.x-style repository layout.
     * 
     * @parameter expression="${repositoryLayout}" default-value="default"
     * @required
     */
    private String repositoryLayout;

    /**
     * Map that contains the layouts
     *
     * @component role="org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout"
     */
    @SuppressWarnings("unchecked")
    private Map repositoryLayouts;

    /**
     * URL where the artifact will be deployed. <br/>
     * ie ( file://C:\m2-repo or scp://host.com/path/to/repo )
     *
     * @parameter expression="${url}"
     * @required
     */
    private String url;

    /**
     * Whether to deploy snapshots with a unique version or not.
     *
     * @parameter expression="${uniqueVersion}" default-value="true"
     */
    private boolean uniqueVersion;
    
    /**
     * @parameter expression="${component.org.apache.maven.artifact.deployer.ArtifactDeployer}"
     * @required
     * @readonly
     */
    private ArtifactDeployer deployer;

    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * @component
     * @required
     * @readonly
     */
    private ArtifactRepositoryFactory repositoryFactory;


    @Override
    public void installArtifact( File file, Artifact artifact )
    {
        ArtifactRepositoryLayout layout;

        layout = ( ArtifactRepositoryLayout ) repositoryLayouts.get( repositoryLayout );

        
        ArtifactRepository deploymentRepository =
            repositoryFactory.createDeploymentArtifactRepository( repositoryId, url, layout, uniqueVersion );

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
