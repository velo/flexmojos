package org.sonatype.flexmojos.matcher.artifact;

import org.apache.maven.artifact.Artifact;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

class VersionMatcher
    extends TypeSafeMatcher<Artifact>
{

    private String version;

    VersionMatcher( String version )
    {
        if ( version == null )
        {
            throw new IllegalArgumentException( "version must be not null" );
        }
        this.version = version;
    }

    @Override
    public boolean matchesSafely( Artifact a )
    {
        return version.equals( a.getVersion() );
    }

    public void describeTo( Description msg )
    {
        msg.appendText( " version " );
        msg.appendValue( version );
    }

}
