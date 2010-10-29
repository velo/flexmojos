package org.sonatype.flexmojos.plugin;

import java.util.List;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;

@Component( role = AbstractMavenLifecycleParticipant.class, hint = "ChecksumEnforcer" )
public class ChecksumEnforcerMavenExtension
    extends AbstractMavenLifecycleParticipant
{

    public void afterProjectsRead( MavenSession session )
    {
        if ( session.getRequest().getUserProperties().containsKey( "flexmojos.ignore.broken.artifacts" ) )
        {
            return;
        }

        List<ArtifactRepository> repos = session.getRequest().getRemoteRepositories();
        for ( ArtifactRepository repo : repos )
        {
            // flex sdk has many dependencies, and all must be intact in order to get flexmojos working
            repo.getReleases().setChecksumPolicy( ArtifactRepositoryPolicy.CHECKSUM_POLICY_FAIL );
            repo.getSnapshots().setChecksumPolicy( ArtifactRepositoryPolicy.CHECKSUM_POLICY_FAIL );
        }
    }
}
