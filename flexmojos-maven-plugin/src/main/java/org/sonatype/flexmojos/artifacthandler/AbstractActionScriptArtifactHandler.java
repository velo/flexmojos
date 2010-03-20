package org.sonatype.flexmojos.artifacthandler;

import org.apache.maven.artifact.handler.DefaultArtifactHandler;

public abstract class AbstractActionScriptArtifactHandler
    extends DefaultArtifactHandler
{

    private static final String ACTION_SCRIPT = "actionScript";

    public String getExtension()
    {
        return getType();
    }

    public String getLanguage()
    {
        return ACTION_SCRIPT;
    }

    public String getPackaging()
    {
        return getType();
    }

    public boolean isAddedToClasspath()
    {
        return false;
    }

    public boolean isIncludesDependencies()
    {
        return false;
    }

}