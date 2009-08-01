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
package org.sonatype.flexmojos.tests.issues;

import java.io.File;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Flexmojos147Test
    extends AbstractIssueTest
{

    @SuppressWarnings( "unchecked" )
    @Test
    public void m2Home()
        throws Exception
    {
        File testDir = getProject( "/issues/flexmojos-147" );
        test( testDir, "eclipse:clean" );
        test( testDir, "flexmojos:flexbuilder" );

        SAXReader reader = new SAXReader();
        Document document = reader.read( new File( testDir, ".actionScriptProperties" ) );

        List<Node> nodes =
            document.selectNodes( "/actionScriptProperties/compiler/libraryPath/libraryPathEntry[@kind=3]" );

        if ( nodes.size() != 13 )
        {
            Assert.fail( "Expected 13 ${M2_HOME} macros, but found " + nodes.size() );
        }

        for ( Node node : nodes )
        {
            String path = node.valueOf( "@path" );
            if ( !path.startsWith( "${M2_HOME}" ) )
            {
                Assert.fail( "${M2_HOME} prefix not found for: " + path );
            }
        }
    }

}
