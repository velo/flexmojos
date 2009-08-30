package org.sonatype.flexmojos.generator.iface.model;

import java.util.Arrays;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias( Excludes.NAME )
public class Excludes
{

    public static final String NAME = "excludes";

    @XStreamImplicit( itemFieldName = "exclude" )
    private List<Definition> excludes;

    public Excludes()
    {
        super();
    }

    public Excludes( List<Definition> excludes )
    {
        this();
        this.excludes = excludes;
    }

    public Excludes( Definition... excludes )
    {
        this( Arrays.asList( excludes ) );
    }

    public List<Definition> getExcludes()
    {
        return excludes;
    }

    public void setIgnores( List<Definition> excludes )
    {
        this.excludes = excludes;
    }

}
