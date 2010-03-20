package org.sonatype.flexmojos.compiler.attributes;


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
