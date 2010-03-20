package org.sonatype.flexmojos.plugin.compiler.attributes;

import org.sonatype.flexmojos.compiler.IDefaultSize;

public class MavenDefaultSize
    implements IDefaultSize
{

    private String height;

    private String width;

    public String height()
    {
        return height;
    }

    public String width()
    {
        return width;
    }

}
