package org.sonatype.flexmojos.common;

import org.sonatype.flexmojos.compiler.MavenArtifact;

public class MavenExtension
{

    private MavenArtifact extensionArtifact;

    private String[] parameters;

    public MavenArtifact getExtensionArtifact()
    {
        return extensionArtifact;
    }

    public String[] getParameters()
    {
        return parameters;
    }

}
