package org.sonatype.flexmojos.generator.iface.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias( "method" )
public class MethodSignature
{

    private String name;

    @XStreamImplicit( itemFieldName = "arg" )
    private List<Class<?>> args;

    public MethodSignature()
    {
        super();
    }

    public MethodSignature( String name, List<Class<?>> args )
    {
        this();
        this.name = name;
        this.args = args;
    }

    public MethodSignature( String name, Class<?>... args )
    {
        this( name, Arrays.asList( args ) );
    }

    public MethodSignature( Method method )
    {
        this( method.getName(), method.getParameterTypes() );
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public List<Class<?>> getArgs()
    {
        if ( args == null )
        {
            this.args = new ArrayList<Class<?>>();
        }
        return args;
    }

    public void setArgs( List<Class<?>> args )
    {
        this.args = args;
    }

    public Class<?>[] getArgsArray()
    {
        return getArgs().toArray( new Class<?>[0] );
    }

    @Override
    public int hashCode()
    {
        final int prime = 59;
        int result = 1;
        result = prime * result + ( ( args == null ) ? 0 : args.hashCode() );
        result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        MethodSignature other = (MethodSignature) obj;
        if ( args == null )
        {
            if ( other.args != null )
                return false;
        }
        else if ( !args.equals( other.args ) )
            return false;
        if ( name == null )
        {
            if ( other.name != null )
                return false;
        }
        else if ( !name.equals( other.name ) )
            return false;
        return true;
    }

}
