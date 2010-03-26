package org.sonatype.flexmojos.matcher.artifact;

import org.apache.maven.artifact.Artifact;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

class VersionMatcher
    extends TypeSafeMatcher<Artifact>
{

    private Matcher<String> versionMatcher;

    VersionMatcher( String version )
    {
        this( CoreMatchers.equalTo( version ) );
    }

    VersionMatcher( Matcher<String> versionMatcher )
    {
        if ( versionMatcher == null )
        {
            throw new IllegalArgumentException( "versionMatcher must be not null" );
        }
        this.versionMatcher = versionMatcher;
    }

    @Override
    public boolean matchesSafely( Artifact a )
    {
        return versionMatcher.matches( a.getVersion() );
    }

    public void describeTo( Description msg )
    {
        msg.appendText( " version " );
        msg.appendDescriptionOf( versionMatcher );
    }

}
