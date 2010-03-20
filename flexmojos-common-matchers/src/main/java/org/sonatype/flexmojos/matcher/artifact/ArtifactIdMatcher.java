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
