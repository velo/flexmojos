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
package net.flexmojos.oss.tests.concept;

import java.io.File;
import java.util.zip.ZipFile;

import net.flexmojos.oss.test.FMVerifier;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CopyMojoTest
    extends AbstractConceptTest
{

    @Test
    public void copyFlexResources()
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
        Assert.assertNotNull( war.getEntry( "rsls/framework-" +
                getArtifactVersion(getFlexFrameworkGroupId(), "framework") + ".swf" ),
                "Rsl entry not present at war!" );
    }

    @Test
    public void copyFlexResourcesWithHashRsls()
            throws Exception
    {
        final File testDir = getProject( "/concept/copy-flex-resources-with-hash" );
        test( testDir, "install", "-DconfigurationReport" );

        final File warFile = new File( testDir, "war/target/copy-war-1.0-SNAPSHOT.war" );
        Assert.assertTrue( warFile.exists(), "War file not found!" );

        final ZipFile war = new ZipFile( warFile );
        Assert.assertNotNull( war.getEntry( "copy-swf-1.0-SNAPSHOT.swf" ), "Swf entry not present at war!" );
        Assert.assertNotNull( war.getEntry( "copy-swf-1.0-SNAPSHOT-module1.swf" ), "Swf entry not present at war!" );
        Assert.assertNotNull( war.getEntry( "copy-swf-1.0-SNAPSHOT-module2.swf" ), "Swf entry not present at war!" );

        final FMVerifier swfVerifier = getVerifier(new File(testDir, "swf"));
        final Xpp3Dom appConfigReportDOM = getFlexConfigReport(swfVerifier, "copy-swf", "1.0-SNAPSHOT");
        final Xpp3Dom rslPath = appConfigReportDOM.getChild("runtime-shared-library-path");

        final File librarySwcFile = new File(rslPath.getChild("path-element").getValue());
        final byte[] artifactBytes = FileUtils.readFileToByteArray(librarySwcFile);
        final String hash = DigestUtils.md5Hex(artifactBytes);

        Assert.assertNotNull( war.getEntry( "rsls/framework-" +
                        getArtifactVersion(getFlexFrameworkGroupId(), "framework") + "-" + hash + ".swf" ),
                "Rsl entry not present at war!" );
    }

}
