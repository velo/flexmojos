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

import org.apache.maven.it.Verifier;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.flexmojos.utilities.MavenUtils;
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
        Verifier v = test( testDir, "compile", "-Dflexmojos.skip=true" );

        String log = FileUtils.fileRead( new File( v.getBasedir(), v.getLogFileName() ) );
        Assert.assertTrue( log.contains( "Skipping Flexmojos execution" ) );
        Assert.assertFalse( log.contains( "Flexmojos " + MavenUtils.getFlexMojosVersion()
            + " - Apache License (NO WARRANTY) - See COPYRIGHT file" ) );

        File target = new File( v.getBasedir(), "target" );
        File swf = new File( target, "flexmojos-168-1.0-SNAPSHOT.swf" );
        Assert.assertFalse( swf.exists() );

        v.assertArtifactNotPresent( "info.rvin.itest.issues", "flexmojos-168", "1.0-SNAPSHOT", "swf" );
    }

    @Test
    public void classifier()
        throws Exception
    {
        Verifier v = testIssue( "flexmojos-168/classifier" );

        File target = new File( v.getBasedir(), "target" );
        String filename = "flexmojos-168-1.0-SNAPSHOT-validation.swf";
        File swf = new File( target, filename );
        Assert.assertTrue( swf.exists() );

        File fakeRepo = new File( getProperty( "fake-repo" ) );
        File artifact = new File( fakeRepo, "info/rvin/itest/issues/flexmojos-168/1.0-SNAPSHOT/" + filename );
        Assert.assertTrue( artifact.exists() );
    }

}
