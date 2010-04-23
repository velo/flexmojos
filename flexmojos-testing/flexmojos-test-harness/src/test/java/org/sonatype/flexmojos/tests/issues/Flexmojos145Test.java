/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    @Test
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
