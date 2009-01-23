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
package org.sonatype.flexmojos.sandbox.bundlepublisher.model;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.flexmojos.sandbox.bundlepublisher.util.Xpp3Util;

public class ArtifactDependency
{

    private Xpp3Dom dom;

    public ArtifactDependency( Xpp3Dom dom )
    {
        this.dom = dom;
    }

    public String getGroupId()
    {
        return Xpp3Util.getValue( dom, "groupId" );
    }

    public String getArtifactId()
    {
        return Xpp3Util.getValue( dom, "artifactId" );
    }

    public String getVersion()
    {
        return Xpp3Util.getValue( dom, "version" );
    }

    public String getClassifier()
    {
        return Xpp3Util.getValue( dom, "classifier" );
    }

    public String getType()
    {
        return Xpp3Util.getValue( dom, "type" );
    }

    public String getScope()
    {
        return Xpp3Util.getValue( dom, "scope" );
    }
}
