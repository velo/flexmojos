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
package org.sonatype.flexmojos.tests.concept;

import java.io.File;
import java.util.zip.ZipFile;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CopyMojoTest
    extends AbstractConceptTest
{

    @Test
    public void copyFlexResouces()
        throws Exception
    {
        File testDir = getProject( "/concept/copy-flex-resources" );
        test( testDir, "install" );

        File warFile = new File( testDir, "war/target/copy-war-1.0-SNAPSHOT.war" );
        Assert.assertTrue( warFile.exists(), "War file not found!" );

        ZipFile war = new ZipFile( warFile );
        Assert.assertNotNull( war.getEntry( "copy-swf-1.0-SNAPSHOT.swf" ), "Swf entry not present at war!" );
        Assert.assertNotNull( war.getEntry( "copy-swf-1.0-SNAPSHOT-module1.swf" ), "Swf entry not present at war!" );
        Assert.assertNotNull( war.getEntry( "copy-swf-1.0-SNAPSHOT-module2.swf" ), "Swf entry not present at war!" );
        Assert.assertNotNull( war.getEntry( "rsls/framework-3.2.0.3958.swf" ), "Rsl entry not present at war!" );
    }

}
