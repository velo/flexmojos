package net.flexmojos.oss.plugin.air.packager;

import net.flexmojos.oss.plugin.RuntimeMavenResolutionException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.repository.RepositorySystem;

import java.util.List;

/**
 * Created by christoferdutz on 17.07.15.
 */
public class Resolver {

    protected RepositorySystem repositorySystem;
    protected ArtifactRepository localRepository;
    protected List<ArtifactRepository> remoteRepositories;

    public Resolver(RepositorySystem repositorySystem, ArtifactRepository localRepository,
                    List<ArtifactRepository> remoteRepositories) {
        this.repositorySystem = repositorySystem;
        this.localRepository = localRepository;
        this.remoteRepositories = remoteRepositories;
    }

    public Artifact resolve(String groupId, String artifactId, String version, String classifier, String type )
            throws RuntimeMavenResolutionException
    {
        Artifact artifact =
                repositorySystem.createArtifactWithClassifier(groupId, artifactId, version, type, classifier);
        if ( !artifact.isResolved() )
        {
            ArtifactResolutionRequest req = new ArtifactResolutionRequest();
            req.setArtifact( artifact );
            req.setLocalRepository( localRepository );
            req.setRemoteRepositories( remoteRepositories );
            ArtifactResolutionResult res = repositorySystem.resolve( req );
            if ( !res.isSuccess() )
            {
                throw new RuntimeMavenResolutionException( "Failed to resolve artifact " + artifact, res, artifact );
            }
        }
        return artifact;
    }

}
