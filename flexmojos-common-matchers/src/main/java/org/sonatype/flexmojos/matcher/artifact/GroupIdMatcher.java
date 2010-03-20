package org.sonatype.flexmojos.matcher.artifact;

import org.apache.maven.artifact.Artifact;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class GroupIdMatcher
    extends TypeSafeMatcher<Artifact>
{

    private String groupId;

    GroupIdMatcher( String groupId )
    {
        if ( groupId == null )
        {
            throw new IllegalArgumentException( "GroupID must be not null" );
        }
        this.groupId = groupId;
    }

    @Override
    public boolean matchesSafely( Artifact a )
    {
        return groupId.equals( a.getGroupId() );
    }

    public void describeTo( Description msg )
    {
        msg.appendText( " groupId " );
        msg.appendValue( groupId );
    }

}
