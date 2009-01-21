package org.sonatype.flexmojos.sandbox.bundlepublisher.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    private List<BundleArtifact> artifacts;

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

    public static BundleDescriptor read( InputStream bundleDescriptor )
        throws FileNotFoundException, XmlPullParserException, IOException
    {
        Xpp3Dom dom = Xpp3DomBuilder.build( new InputStreamReader( bundleDescriptor ) );
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

    public List<BundleArtifact> getArtifacts()
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
                artifacts = new ArrayList<BundleArtifact>();
                for ( Xpp3Dom artifactDom : dom.getChildren( "artifact" ) )
                {
                    artifacts.add( new BundleArtifact( artifactDom ) );
                }
            }
        }
        return artifacts;
    }

}
