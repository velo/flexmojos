package org.sonatype.flexmojos.common.matcher;

import org.apache.maven.artifact.Artifact;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

class TypeMatcher
    extends TypeSafeMatcher<Artifact>
{

    private String type;

    TypeMatcher( String type )
    {
        if ( type == null )
        {
            throw new IllegalArgumentException( "type must be not null" );
        }
        this.type = type;
    }

    @Override
    public boolean matchesSafely( Artifact a )
    {
        return type.equals( a.getType() );
    }

    public void describeTo( Description msg )
    {
        msg.appendText( " type " );
        msg.appendValue( type );
    }

}
