package org.sonatype.flexmojos.sandbox.bundlepublisher.model;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.flexmojos.sandbox.bundlepublisher.util.Xpp3Util;

public class Organization
{

    private Xpp3Dom dom;

    Organization( Xpp3Dom dom )
    {
        this.dom = dom;
    }

    public String getName()
    {
        return Xpp3Util.getValue( dom, "name" );
    }

    public String getLicense()
    {
        return Xpp3Util.getValue( dom, "license" );
    }

    public String getUrl()
    {
        return Xpp3Util.getValue( dom, "url" );
    }

}
