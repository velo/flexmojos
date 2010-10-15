package org.sonatype.flexmojos.matcher.artifact;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.artifactId;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.classifier;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.groupId;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.scope;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.type;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.version;

import org.apache.maven.artifact.Artifact;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ArtifactMatcherTest
{

    private Artifact artifact;

    @BeforeMethod
    public void createArtifact()
    {
        artifact = mock( Artifact.class );
    }

    @Test
    public void testArtifactIdString()
    {
        String value = "artifactId";
        when( artifact.getArtifactId() ).thenReturn( value );
        assertThat( artifact, artifactId( value ) );
    }

    @Test
    public void testClassifierString()
    {
        String value = "classifier";
        when( artifact.getClassifier() ).thenReturn( value );
        assertThat( artifact, classifier( value ) );
    }

    @Test
    public void testGroupIdString()
    {
        String value = "groupId";
        when( artifact.getGroupId() ).thenReturn( value );
        assertThat( artifact, groupId( value ) );
    }

    @Test
    public void testScopeString()
    {
        String value = "scope";
        when( artifact.getScope() ).thenReturn( value );
        assertThat( artifact, scope( value ) );
    }

    @Test
    public void testTypeString()
    {
        String value = "type";
        when( artifact.getType() ).thenReturn( value );
        assertThat( artifact, type( value ) );
    }

    @Test
    public void testVersionString()
    {
        String value = "version";
        when( artifact.getVersion() ).thenReturn( value );
        assertThat( artifact, version( value ) );
    }

    @Test( expectedExceptions = { AssertionError.class } )
    public void negativeTest()
    {
        String value = "version";
        when( artifact.getVersion() ).thenReturn( value );
        assertThat( artifact, version( "europe" ) );
    }

}
