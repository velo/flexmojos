package org.sonatype.flexmojos.sandbox.bundlepublisher.util;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public class Xpp3Util
{

    public static String getValue( Xpp3Dom dom, String name )
    {
        dom = dom.getChild( name );
        if ( dom == null )
        {
            return null;
        }
        return dom.getValue();
    }

}
