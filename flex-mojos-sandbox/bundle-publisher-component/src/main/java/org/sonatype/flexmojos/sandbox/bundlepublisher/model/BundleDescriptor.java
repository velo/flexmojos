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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class BundleDescriptor
{

    private Xpp3Dom dom;

    private Organization organization;

    private Defaults defaults;

    private List<Artifact> artifacts;

    private BundleDescriptor( Xpp3Dom dom )
    {
        this.dom = dom;
    }

    public static BundleDescriptor read( File bundleDescriptor )
        throws FileNotFoundException, XmlPullParserException, IOException
    {
        Xpp3Dom dom = Xpp3DomBuilder.build( new FileReader( bundleDescriptor ) );
        return new BundleDescriptor( dom );
    }

    public Organization getOrganization()
    {
        if ( organization == null )
        {
            Xpp3Dom dom = this.dom.getChild( "organization" );
            if ( dom == null )
            {
                return null;
            }
            organization = new Organization( dom );
        }
        return organization;
    }

    public Defaults getDefaults()
    {
        if ( defaults == null )
        {
            Xpp3Dom dom = this.dom.getChild( "defaults" );
            if ( dom == null )
            {
                return null;
            }
            defaults = new Defaults( dom );
        }
        return defaults;
    }

    public List<Artifact> getArtifacts()
    {
        if ( artifacts == null )
        {
            Xpp3Dom dom = this.dom.getChild( "artifacts" );
            if ( dom == null )
            {
                artifacts = Collections.emptyList();
            }
            else
            {
                artifacts = new ArrayList<Artifact>();
                for ( Xpp3Dom artifactDom : dom.getChildren( "artifact" ) )
                {
                    artifacts.add( new Artifact( artifactDom ) );
                }
            }
        }
        return artifacts;
    }

}
