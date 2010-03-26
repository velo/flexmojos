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
