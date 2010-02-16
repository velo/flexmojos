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

    public static TypeMatcher type( String type )
    {
        return new TypeMatcher( type );
    }

    public static ClassifierMatcher classifier( String classifier )
    {
        return new ClassifierMatcher( classifier );
    }
    
    public static ScopeMatcher scope( String scope)
    {
        return new ScopeMatcher( scope );
    }
}
