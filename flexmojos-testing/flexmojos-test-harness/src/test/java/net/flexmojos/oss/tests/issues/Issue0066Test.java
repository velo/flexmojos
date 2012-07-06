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
package net.flexmojos.oss.tests.issues;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;

import org.testng.annotations.Test;

public class Issue0066Test
    extends AbstractIssueTest
{

    @Test( groups = { "generator" } )
    public void issue66()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0066" );
        test( testDir, "install" );

        // Issue 62 test
        // is excluded!
        File another = new File( testDir, "flex/src/main/flex/net/flexmojos/oss/generator/AnotherPojo.as" );
        assertFalse( "File not found " + another, another.isFile() );

        // Issue 65 test
        File pojo = new File( testDir, "flex/src/main/flex/net/flexmojos/oss/generator/SimplePojo.as" );
        assertTrue( "File not found " + pojo, pojo.isFile() );
        File base =
            new File( testDir,
                      "flex/target/generated-sources/flexmojos/net/flexmojos/oss/generator/SimplePojoBase.as" );
        assertTrue( "File not found " + base, base.isFile() );
    }

}
