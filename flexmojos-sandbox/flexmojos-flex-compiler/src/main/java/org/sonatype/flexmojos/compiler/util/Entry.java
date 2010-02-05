package org.sonatype.flexmojos.compiler.util;

public class Entry<E, F>
{

    private E name;

    private F value;

    public Entry( E name, F value )
    {
        super();
        this.name = name;
        this.value = value;
    }

    public E getName()
    {
        return name;
    }

    public F getValue()
    {
        return value;
    }

    public void setName( E name )
    {
        this.name = name;
    }

    public void setValue( F value )
    {
        this.value = value;
    }
}
