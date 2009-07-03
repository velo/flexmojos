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
package org.sonatype.flexmojos.utilities;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Map;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MavenUtilsTest
{

    private String XML = "<flex-config>" + //
        "<compiler>" + //
        "<namespaces>" + //
        "<namespace>" + //
        "<uri>http://www.adobe.com/2006/mxml</uri>" + //
        "<manifest>mxml-manifest.xml</manifest>" + //
        "</namespace>" + //
        "</namespaces>" + //
        "</compiler>" + //
        "</flex-config>";//

    @Test
    public void testReadNamespaces()
        throws Exception
    {
        SAXBuilder parser = new SAXBuilder();
        Document document = parser.build( new ByteArrayInputStream( XML.getBytes() ) );

        Map<String, File> namespaces = MavenUtils.readNamespaces( new File( "./" ), document );
        Assert.assertEquals( 1, namespaces.size() );
    }

}
