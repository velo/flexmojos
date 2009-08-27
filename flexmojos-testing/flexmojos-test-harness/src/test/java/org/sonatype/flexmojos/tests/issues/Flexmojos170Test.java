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

import org.testng.Assert;
import org.testng.annotations.Test;

public class Flexmojos170Test
    extends AbstractIssueTest
{

    @Test
    public void generateSwfConfig()
        throws Exception
    {
        File testDir = getProject( "/issues/flexmojos-170/swf" );

        test( testDir, "org.sonatype.flexmojos:flexmojos-maven-plugin:" + getFlexmojosVersion()
            + ":generate-config-swf" );

        checkReport( testDir );
    }

    @Test
    public void generateSwcConfig()
        throws Exception
    {
        File testDir = getProject( "/issues/flexmojos-170/swc" );

        test( testDir, "org.sonatype.flexmojos:flexmojos-maven-plugin:" + getFlexmojosVersion()
            + ":generate-config-swc" );

        checkReport( testDir );
    }

    private void checkReport( File testDir )
    {
        File target = new File( testDir, "target" );
        File configReport = new File( target, "flexmojos-170-1.0-SNAPSHOT-config-report.xml" );
        Assert.assertTrue( configReport.isFile() );
    }

}
