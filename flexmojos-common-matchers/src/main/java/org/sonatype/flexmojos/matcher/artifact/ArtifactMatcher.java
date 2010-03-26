package org.sonatype.flexmojos.matcher.artifact;

import org.hamcrest.Matcher;

public class ArtifactMatcher
{

    public static GroupIdMatcher groupId( String groupId )
    {
        return new GroupIdMatcher( groupId );
    }

    public static ArtifactIdMatcher artifactId( String artifactId )
    {
        return new ArtifactIdMatcher( artifactId );
    }

    public static VersionMatcher version( String version )
    {
        return new VersionMatcher( version );
    }

    public static VersionMatcher version( Matcher<String> versionMatcher )
    {
        return new VersionMatcher( versionMatcher );
    }

    public static TypeMatcher type( String type )
    {
        return new TypeMatcher( type );
    }

    public static ClassifierMatcher classifier( String classifier )
    {
        return new ClassifierMatcher( classifier );
    }

    public static ScopeMatcher scope( String scope )
    {
        return new ScopeMatcher( scope );
    }
}
