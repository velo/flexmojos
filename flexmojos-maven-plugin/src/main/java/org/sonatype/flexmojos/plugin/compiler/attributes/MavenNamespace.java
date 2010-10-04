package org.sonatype.flexmojos.plugin.compiler.attributes;

import java.io.File;

import org.sonatype.flexmojos.compiler.INamespace;
import org.sonatype.flexmojos.util.PathUtil;

public class MavenNamespace
    implements INamespace
{

    private String uri;

    private File manifest;

    public MavenNamespace()
    {
        super();
    }

    public MavenNamespace( String uri, File manifest )
    {
        super();
        this.uri = uri;
        this.manifest = manifest;
    }

    public String manifest()
    {
        return PathUtil.path( manifest );
    }

    public String uri()
    {
        return uri;
    }

}
