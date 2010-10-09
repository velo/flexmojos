package org.sonatype.flexmojos.plugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;

public class RuntimeMavenResolutionException
    extends RuntimeException
{

    private Artifact artifact;

    private ArtifactResolutionResult resolutionResult;

    public RuntimeMavenResolutionException( String msg, ArtifactResolutionResult res, Artifact artifact )
    {
        super( msg );
        this.resolutionResult = res;
        this.artifact = artifact;
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

    public ArtifactResolutionResult getResolutionResult()
    {
        return resolutionResult;
    }

}
