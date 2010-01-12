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
