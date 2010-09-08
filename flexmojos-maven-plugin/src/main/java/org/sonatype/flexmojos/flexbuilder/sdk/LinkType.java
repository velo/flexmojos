package org.sonatype.flexmojos.flexbuilder.sdk;

public enum LinkType
{
    MERGE( 1 ), EXTERNAL( 2 ), RSL( 3 ), RSL_DIGEST( 4 );

    private int id;

    private LinkType( int id )
    {
        this.id = id;
    }

    public int getId()
    {
        return id;
    }
}
