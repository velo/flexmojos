package org.sonatype.flexmojos.matcher.artifact;

import static org.hamcrest.Matchers.equalTo;

import org.apache.maven.artifact.Artifact;
import org.hamcrest.Matcher;

public class ArtifactMatcher
{
    private static class ArtifactIdMatcher
        extends AbstractArtifactMatcher<Artifact>
    {
        ArtifactIdMatcher( Matcher<? extends String> elementMatcher )
        {
            super( elementMatcher, "artifactId" );
        }

        @Override
        protected String getValue( Artifact item )
        {
            return item.getArtifactId();
        }
    }

    private static class ClassifierMatcher
        extends AbstractArtifactMatcher<Artifact>
    {
        public ClassifierMatcher( Matcher<? extends String> elementMatcher )
        {
            super( elementMatcher, "classifier" );
        }

        @Override
        protected String getValue( Artifact item )
        {
            return item.getClassifier();
        }
    }

    private static class GroupIdMatcher
        extends AbstractArtifactMatcher<Artifact>
    {
        public GroupIdMatcher( Matcher<? extends String> elementMatcher )
        {
            super( elementMatcher, "groupId" );
        }

        @Override
        protected String getValue( Artifact item )
        {
            return item.getGroupId();
        }
    }

    private static class ScopeMatcher
        extends AbstractArtifactMatcher<Artifact>
    {
        public ScopeMatcher( Matcher<? extends String> elementMatcher )
        {
            super( elementMatcher, "scope" );
        }

        @Override
        protected String getValue( Artifact item )
        {
            return item.getScope();
        }
    }

    private static class TypeMatcher
        extends AbstractArtifactMatcher<Artifact>
    {
        public TypeMatcher( Matcher<? extends String> elementMatcher )
        {
            super( elementMatcher, "type" );
        }

        @Override
        protected String getValue( Artifact item )
        {
            return item.getType();
        }
    }

    private static class VersionMatcher
        extends AbstractArtifactMatcher<Artifact>
    {
        public VersionMatcher( Matcher<? extends String> elementMatcher )
        {
            super( elementMatcher, "version" );
        }

        @Override
        protected String getValue( Artifact item )
        {
            return item.getVersion();
        }
    }

    static
    {
        new ArtifactMatcher();
    }

    public static ArtifactIdMatcher artifactId( Matcher<? extends String> artifactId )
    {
        return new ArtifactIdMatcher( artifactId );
    }

    public static ArtifactIdMatcher artifactId( String artifactId )
    {
        return artifactId( equalTo( artifactId ) );
    }

    public static ClassifierMatcher classifier( Matcher<? extends String> classifier )
    {
        return new ClassifierMatcher( classifier );
    }

    public static ClassifierMatcher classifier( String classifier )
    {
        return classifier( equalTo( classifier ) );
    }

    public static GroupIdMatcher groupId( Matcher<? extends String> groupId )
    {
        return new GroupIdMatcher( groupId );
    }

    public static GroupIdMatcher groupId( String groupId )
    {
        return groupId( equalTo( groupId ) );
    }

    public static ScopeMatcher scope( Matcher<? extends String> scope )
    {
        return new ScopeMatcher( scope );
    }

    public static ScopeMatcher scope( String scope )
    {
        return scope( equalTo( scope ) );
    }

    public static TypeMatcher type( Matcher<? extends String> type )
    {
        return new TypeMatcher( type );
    }

    public static TypeMatcher type( String type )
    {
        return type( equalTo( type ) );
    }

    public static VersionMatcher version( Matcher<? extends String> versionMatcher )
    {
        return new VersionMatcher( versionMatcher );
    }

    public static VersionMatcher version( String version )
    {
        return version( equalTo( version ) );
    }

    private ArtifactMatcher()
    {
        super();
    }

}
