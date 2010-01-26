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
package org.sonatype.flexmojos.tests.coverage;

import static org.testng.AssertJUnit.assertTrue;

import java.io.File;

import org.testng.annotations.Test;

public class HtmlWrapperTemplateTest
    extends AbstractCoverageTest
{

    @Test
    public void testHtmlwrapperTemplates()
        throws Exception
    {
        File testDir = getProject( "/coverage/htmlwrapper-templates" );
        test( testDir, "package" );
        File folder = new File( testDir, "folder/target/htmlwrapper-templates-folder-1.0-SNAPSHOT.html" );
        assertTrue( "Should generate htmlwrapper" + folder.getAbsolutePath(), folder.exists() );
        File zip = new File( testDir, "zip/target/htmlwrapper-templates-zip-1.0-SNAPSHOT.html" );
        assertTrue( "Should generate htmlwrapper " + zip.getAbsolutePath(), zip.exists() );
    }

}
