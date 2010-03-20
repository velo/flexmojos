package org.sonatype.flexmojos.matcher.artifact;

import org.apache.maven.artifact.Artifact;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

class ScopeMatcher
    extends TypeSafeMatcher<Artifact>
{

    private String scope;

    ScopeMatcher( String scope )
    {
        this.scope = scope;
    }

    @Override
    public boolean matchesSafely( Artifact a )
    {
        if ( scope == null )
        {
            return a.getScope() == null;
        }
        return scope.equals( a.getScope() );
    }

    public void describeTo( Description msg )
    {
        msg.appendText( " scope " );
        msg.appendValue( scope );
    }

}
