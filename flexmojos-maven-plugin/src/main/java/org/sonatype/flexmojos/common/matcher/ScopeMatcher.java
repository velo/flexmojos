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
package org.sonatype.flexmojos.common.matcher;

import org.apache.maven.artifact.Artifact;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

class ScopeMatcher
    extends TypeSafeMatcher<Artifact>
{

    private String scope;

    ScopeMatcher( String scope )
    {
        this.scope = scope;
    }

    @Override
    public boolean matchesSafely( Artifact a )
    {
        if ( scope == null )
        {
            return a.getScope() == null;
        }
        return scope.equals( a.getScope() );
    }

    public void describeTo( Description msg )
    {
        msg.appendText( " scope " );
        msg.appendValue( scope );
    }

}
