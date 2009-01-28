/**
 * Copyright 2008 Marvin Herman Froeder
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.sandbox.bundlepublisher.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.flexmojos.sandbox.bundlepublisher.util.Xpp3Util;

public class BundleArtifact
{

    private Xpp3Dom dom;

    private List<ArtifactDependency> dependencies;

    public BundleArtifact( Xpp3Dom dom )
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

    public String getLocation()
    {
        return Xpp3Util.getValue( dom, "location" );
    }

    public String getType()
    {
        return Xpp3Util.getValue( dom, "type" );
    }

    public String getClassifier()
    {
        return Xpp3Util.getValue( dom, "classifier" );
    }

    public List<ArtifactDependency> getDependencies()
    {
        if ( this.dependencies == null )
        {
            Xpp3Dom dom = this.dom.getChild( "dependencies" );
            if ( dom == null )
            {
                this.dependencies = Collections.emptyList();
            }
            else
            {
                this.dependencies = new ArrayList<ArtifactDependency>();
                for ( Xpp3Dom depDom : dom.getChildren( "dependency" ) )
                {
                    this.dependencies.add( new ArtifactDependency( depDom ) );
                }
            }
        }
        return this.dependencies;
    }

}
