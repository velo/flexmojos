package org.sonatype.flexmojos.plugin.compiler;

import org.sonatype.flexmojos.compiler.IApplicationDomains;

public class MavenApplicationDomains
    implements IApplicationDomains
{

    private String pathElement;

    private String applicationDomain;

    public String pathElement()
    {
        return this.pathElement;
    }

    public String applicationDomain()
    {
        return this.applicationDomain;
    }

}
