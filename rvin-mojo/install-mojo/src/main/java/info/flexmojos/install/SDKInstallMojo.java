package info.flexmojos.install;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.repository.ArtifactRepository;

/**
 * @goal install-sdk
 * @requiresProject false
 * @requiresDirectInvocation true
 * @author marvin
 */
public class SDKInstallMojo
    extends AbstractInstallMojo
{

    /**
     * @component
     */
    private ArtifactInstaller installer;

    /**
     * @parameter expression="${localRepository}"
     */
    private ArtifactRepository localRepository;

    public void installArtifact( File file, Artifact artifact )
    {
        try
        {
            installer.install( file, artifact, localRepository ); // to install
        }
        catch ( ArtifactInstallationException e )
        {
            getLog().error( "Unable to install artifact: " + file.getAbsolutePath(), e );
        }
    }
}
