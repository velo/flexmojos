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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.zip.ZipFile;

import org.sonatype.flexmojos.matcher.file.FileMatcher;
import org.testng.annotations.Test;

public class Flexmojos130Test
    extends AbstractIssueTest
{

    @Test
    public void attachAsdoc()
        throws Exception
    {
        File testDir = getProject( "/issues/flexmojos-130" );
        test( testDir, "install" );

        File swc = new File( testDir, "target/flexmojos-130-1.0-SNAPSHOT.swc" );
        assertThat( swc, FileMatcher.exists() );

        ZipFile swcC = new ZipFile( swc );
        try
        {
            assertNotNull( swcC.getEntry( "docs/ASDoc_Config.xml" ) );
            assertNotNull( swcC.getEntry( "docs/org.sonatype.flexmojos.it.xml" ) );
            assertNotNull( swcC.getEntry( "docs/overviews.xml" ) );
            assertNotNull( swcC.getEntry( "docs/packages.dita" ) );
            assertNotNull( swcC.getEntry( "docs/__Global__.xml" ) );
        }
        finally
        {
            swcC.close();
        }
    }
}
