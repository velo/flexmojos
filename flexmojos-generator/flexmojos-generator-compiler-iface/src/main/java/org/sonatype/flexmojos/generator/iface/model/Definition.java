package org.sonatype.flexmojos.generator.iface.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias( "def" )
public class Definition
{

    private Class<?> classname;

    @XStreamImplicit( itemFieldName = "method" )
    private Set<MethodSignature> methods;

    public Definition()
    {
        super();
    }

    public Definition( Class<?> classname, Collection<MethodSignature> methods )
    {
        this();
        this.classname = classname;
        this.methods = new LinkedHashSet<MethodSignature>( methods );
    }

    public Definition( Class<?> classname, MethodSignature... methods )
    {
        this( classname, Arrays.asList( methods ) );
    }

    public Class<?> getClassname()
    {
        return classname;
    }

    public void setClassname( Class<?> classname )
    {
        this.classname = classname;
    }

    public Set<MethodSignature> getMethods()
    {
        if ( this.methods == null )
        {
            this.methods = new LinkedHashSet<MethodSignature>();
        }
        return methods;
    }

    public void setMethods( Collection<MethodSignature> methods )
    {
        this.methods = new LinkedHashSet<MethodSignature>( methods );
    }

}
