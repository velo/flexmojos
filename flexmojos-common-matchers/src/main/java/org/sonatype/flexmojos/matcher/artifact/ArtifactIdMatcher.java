package org.sonatype.flexmojos.matcher.artifact;

import org.apache.maven.artifact.Artifact;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class ArtifactIdMatcher
    extends TypeSafeMatcher<Artifact>
{

    private String artifactId;

    ArtifactIdMatcher( String artifactId )
    {
        if ( artifactId == null )
        {
            throw new IllegalArgumentException( "artifactId must be not null" );
        }
        this.artifactId = artifactId;
    }

    @Override
    public boolean matchesSafely( Artifact a )
    {
        return artifactId.equals( a.getArtifactId() );
    }

    public void describeTo( Description msg )
    {
        msg.appendText( " artifactId " );
        msg.appendValue( artifactId );
    }

}
