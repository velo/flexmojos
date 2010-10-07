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

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.flexmojos.plugin.utilities.MavenUtils;
import org.sonatype.flexmojos.test.FMVerifier;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Flexmojos168Test
    extends AbstractIssueTest
{

    @Test
    public void skip()
        throws Exception
    {
        File testDir = getProject( "/issues/flexmojos-168/skip" );
        FMVerifier v = test( testDir, "compile", "-Dflexmojos.skip=true" );

        String log = FileUtils.fileRead( new File( v.getBasedir(), v.getLogFileName() ) );
        Assert.assertTrue( log.contains( "Skipping flexmojos goal execution" ) );
        Assert.assertFalse( log.contains( "Flexmojos " + MavenUtils.getFlexMojosVersion()
            + " - Apache License (NO WARRANTY) - See COPYRIGHT file" ) );

        File target = new File( v.getBasedir(), "target" );
        File swf = new File( target, "flexmojos-168-skip-1.0-SNAPSHOT.swf" );
        Assert.assertFalse( swf.exists() );

        v.assertArtifactNotPresent( "info.rvin.itest.issues", "flexmojos-168-skip", "1.0-SNAPSHOT", "swf" );
    }

    @Test
    public void classifier()
        throws Exception
    {
        FMVerifier v = testIssue( "flexmojos-168/classifier" );

        File target = new File( v.getBasedir(), "target" );
        String filename = "flexmojos-168-1.0-SNAPSHOT-validation.swf";
        File swf = new File( target, filename );
        Assert.assertTrue( swf.exists() );

        File fakeRepo = new File( getProperty( "fake-repo" ) );
        File artifact = new File( fakeRepo, "info/rvin/itest/issues/flexmojos-168/1.0-SNAPSHOT/" + filename );
        Assert.assertTrue( artifact.exists() );
    }

}
