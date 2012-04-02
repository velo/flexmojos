/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.flexmojos.oss.matcher.artifact;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static net.flexmojos.oss.matcher.artifact.ArtifactMatcher.artifactId;
import static net.flexmojos.oss.matcher.artifact.ArtifactMatcher.classifier;
import static net.flexmojos.oss.matcher.artifact.ArtifactMatcher.groupId;
import static net.flexmojos.oss.matcher.artifact.ArtifactMatcher.scope;
import static net.flexmojos.oss.matcher.artifact.ArtifactMatcher.type;
import static net.flexmojos.oss.matcher.artifact.ArtifactMatcher.version;

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
