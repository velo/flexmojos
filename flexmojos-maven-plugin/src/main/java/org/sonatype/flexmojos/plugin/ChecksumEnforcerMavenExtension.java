package org.sonatype.flexmojos.plugin;

import java.util.List;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;

@Component( role = AbstractMavenLifecycleParticipant.class, hint = "ChecksumEnforcer" )
public class ChecksumEnforcerMavenExtension
    extends AbstractMavenLifecycleParticipant
{

    @Override
    public void afterProjectsRead( MavenSession session )
        throws MavenExecutionException
    {
        if ( session.getRequest().getUserProperties().containsKey( "flexmojos.ignore.broken.artifacts" ) )
        {
            return;
        }
        
        session.getRequest().setRemoteRepositories( fixRepos( session.getRequest().getRemoteRepositories() ) );

        for ( MavenProject p : session.getProjects() )
        {
            p.setRemoteArtifactRepositories( fixRepos( p.getRemoteArtifactRepositories() ) );
        }
    }

    private List<ArtifactRepository> fixRepos( List<ArtifactRepository> list )
    {
        for ( ArtifactRepository repo : list )
        {
            // flex sdk has many dependencies, and all must be intact in order to get flexmojos working
            repo.getReleases().setChecksumPolicy( ArtifactRepositoryPolicy.CHECKSUM_POLICY_FAIL );
            repo.getSnapshots().setChecksumPolicy( ArtifactRepositoryPolicy.CHECKSUM_POLICY_FAIL );
        }

        return list;
    }
}
