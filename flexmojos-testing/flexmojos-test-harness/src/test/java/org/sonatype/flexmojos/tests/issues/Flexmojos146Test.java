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

public class Flexmojos146Test
    extends AbstractIssueTest
{

    @SuppressWarnings( "unchecked" )
    @Test( enabled = false )
    public void attachSources()
        throws Exception
    {
        File testDir = getProject( "/issues/flexmojos-146" );
        test( testDir, "eclipse:clean" );
        test( testDir, "flexmojos:flexbuilder" );

        final String[] SDK_SOURCES = { "flex", "framework", "rpc", "utilities" };

        SAXReader reader = new SAXReader();
        Document document = reader.read( new File( testDir, ".actionScriptProperties" ) );

        List<Node> nodes =
            document.selectNodes( "/actionScriptProperties/compiler/libraryPath/libraryPathEntry[@sourcepath]" );

        if ( nodes.size() != SDK_SOURCES.length )
        {
            Assert.fail( "Expected " + SDK_SOURCES.length + " applications, but found " + nodes.size() );
        }

        List<String> found = new ArrayList<String>( 10 );
        for ( Node node : nodes )
        {
            found.add( node.valueOf( "@sourcepath" ).split( "/" )[2] );
        }

        List<String> expected = new ArrayList<String>( Arrays.asList( SDK_SOURCES ) );
        if ( !found.containsAll( expected ) )
        {
            List<String> missing = new ArrayList<String>( expected );
            missing.removeAll( found );

            List<String> extras = new ArrayList<String>( found );
            extras.removeAll( expected );

            Assert.fail( "Found " + found + " sources. Found " + found + " sources. Not found " + missing + ". Extra "
                + extras + "." );
        }
    }

}
