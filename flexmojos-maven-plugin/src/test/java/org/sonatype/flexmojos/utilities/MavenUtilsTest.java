/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
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
