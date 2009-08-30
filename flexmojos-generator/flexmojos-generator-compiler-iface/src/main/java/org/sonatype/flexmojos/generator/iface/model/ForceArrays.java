package org.sonatype.flexmojos.generator.iface.model;

import java.util.Arrays;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias( ForceArrays.NAME )
public class ForceArrays
{

    public static final String NAME = "forceArrays";

    @XStreamImplicit( itemFieldName = "forceArray" )
    private List<Definition> signatures;

    public ForceArrays()
    {
        super();
    }

    public ForceArrays( List<Definition> signatures )
    {
        this();
        this.signatures = signatures;
    }

    public ForceArrays( Definition... signatures )
    {
        this( Arrays.asList( signatures ) );
    }

    public List<Definition> getSignatures()
    {
        return signatures;
    }

    public void setSignatures( List<Definition> signatures )
    {
        this.signatures = signatures;
    }

}
