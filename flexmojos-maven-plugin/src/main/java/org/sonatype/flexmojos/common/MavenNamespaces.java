package org.sonatype.flexmojos.common;

import org.sonatype.flexmojos.compiler.INamespace;

public class MavenNamespaces
    implements INamespace
{

    private String manifest;

    private String uri;

    public String manifest()
    {
        return manifest;
    }

    public String uri()
    {
        return uri;
    }

}
