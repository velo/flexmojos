package org.sonatype.flexmojos.plugin.compiler;

import org.sonatype.flexmojos.compiler.IApplicationDomain;

public class MavenApplicationDomains
    implements IApplicationDomain
{

    private String pathElement;

    private String applicationDomain;

    public String pathElement()
    {
        return this.pathElement;
    }

    @Override
    public String applicationDomainTarget()
    {
        return this.applicationDomain;
    }

}
