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
import java.io.InputStream;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.text.StringContains;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Flexmojos247Test
    extends AbstractIssueTest
{

    @Test
    public void includeAsClasses()
        throws Exception
    {
        String baseDir = testIssue( "flexmojos-247" ).getBasedir();
        File target = new File( baseDir, "target" );
        Assert.assertTrue( target.exists() );

        File swc = new File( target, "flexmojos-247-1.0-SNAPSHOT.swc" );
        Assert.assertTrue( swc.exists() );

        String catalog;
        ZipFile zf = new ZipFile( swc );
        try
        {
            InputStream in = zf.getInputStream( zf.getEntry( "catalog.xml" ) );
            catalog = IOUtils.toString( in );
            in.close();
        }
        finally
        {
            zf.close();
        }

        // must have both classes and the uri
        MatcherAssert.assertThat( catalog, StringContains.containsString( "AClass" ) );
        MatcherAssert.assertThat( catalog, StringContains.containsString( "BClass" ) );
        MatcherAssert.assertThat( catalog, StringContains.containsString( "http://flexmojos.sonatype.org/tests" ) );
    }
}
