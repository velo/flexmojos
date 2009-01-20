package org.sonatype.flexmojos.sandbox.bundlepublisher.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.flexmojos.sandbox.bundlepublisher.util.Xpp3Util;

public class Artifact
{

    private Xpp3Dom dom;

    private List<ArtifactDependency> dependencies;

    public Artifact( Xpp3Dom dom )
    {
        this.dom = dom;
    }

    public String getArtifactId()
    {
        return Xpp3Util.getValue( dom, "artifactId" );
    }

    public String getLocation()
    {
        return Xpp3Util.getValue( dom, "location" );
    }

    public String getType()
    {
        return Xpp3Util.getValue( dom, "type" );
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
