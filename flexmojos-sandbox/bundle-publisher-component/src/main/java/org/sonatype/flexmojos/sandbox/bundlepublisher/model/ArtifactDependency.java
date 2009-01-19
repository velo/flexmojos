package org.sonatype.flexmojos.sandbox.bundlepublisher.model;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.flexmojos.sandbox.bundlepublisher.util.Xpp3Util;

public class ArtifactDependency
{

    private Xpp3Dom dom;

    public ArtifactDependency( Xpp3Dom dom )
    {
        this.dom = dom;
    }

    public String getGroupId()
    {
        return Xpp3Util.getValue( dom, "groupId" );
    }

    public String getArtifactId()
    {
        return Xpp3Util.getValue( dom, "artifactId" );
    }

    public String getVersion()
    {
        return Xpp3Util.getValue( dom, "version" );
    }

    public String getClassifier()
    {
        return Xpp3Util.getValue( dom, "classifier" );
    }

    public String getType()
    {
        return Xpp3Util.getValue( dom, "type" );
    }

    public String getScope()
    {
        return Xpp3Util.getValue( dom, "scope" );
    }
}
