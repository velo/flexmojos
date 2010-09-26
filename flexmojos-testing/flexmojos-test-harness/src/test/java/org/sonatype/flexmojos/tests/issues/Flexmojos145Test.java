package org.sonatype.flexmojos.tests.issues;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Flexmojos145Test
    extends AbstractIssueTest
{

    @SuppressWarnings( "unchecked" )
    @Test( enabled = false )
    public void multipleApplications()
        throws Exception
    {
        File testDir = getProject( "/issues/flexmojos-145" );
        test( testDir, "eclipse:clean" );
        test( testDir, "flexmojos:flexbuilder" );

        final String[] APPLICATIONS = { "main.mxml", "additional1.mxml", "additional2.mxml", "additional3.mxml" };

        SAXReader reader = new SAXReader();
        Document document = reader.read( new File( testDir, ".actionScriptProperties" ) );

        List<Node> nodes = document.selectNodes( "/actionScriptProperties/applications/application" );

        if ( nodes.size() != APPLICATIONS.length )
        {
            Assert.fail( "Expected " + APPLICATIONS.length + " applications, but found " + nodes.size() );
        }

        List<String> found = new ArrayList<String>();
        for ( Node node : nodes )
        {
            found.add( node.valueOf( "@path" ) );
        }

        List<String> expected = new ArrayList<String>( Arrays.asList( APPLICATIONS ) );
        if ( !found.containsAll( expected ) )
        {
            List<String> missing = new ArrayList<String>( expected );
            missing.removeAll( found );

            List<String> extras = new ArrayList<String>( found );
            extras.removeAll( expected );

            Assert.fail( "Found " + found + " applications. Not found " + missing + ".  Extra " + extras + "." );
        }
    }

}
