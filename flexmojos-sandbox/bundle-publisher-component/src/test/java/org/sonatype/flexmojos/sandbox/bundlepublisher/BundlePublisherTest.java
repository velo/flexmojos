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
package org.sonatype.flexmojos.sandbox.bundlepublisher;

import java.io.File;
import java.io.FileInputStream;

import org.apache.maven.mercury.plexus.PlexusMercury;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

public class BundlePublisherTest
    extends org.codehaus.plexus.PlexusTestCase
{

    private static File root;

    public void setUp()
        throws Exception
    {
        super.setUp();

        root = new File( "./target/test-classes" );
    }

    public void testInstallM2Repo()
        throws Exception
    {
        PlexusMercury mercury = (PlexusMercury) lookup( PlexusMercury.ROLE );

        File repoDir = new File( root, "repo" );
        LocalRepositoryM2 repo = mercury.constructLocalRepositoryM2( "test", repoDir, null, null, null, null );

        BundlePublisher publisher = (BundlePublisher) lookup( BundlePublisher.ROLE );
        publisher.publish( new File( root, "bundle.zip" ), new FileInputStream( new File( root, "descriptor.xml" ) ),
                           repo );

        assertExits( new File( repoDir, "org/sonatype/test/deeper/1.0.1/deeper-1.0.1.pom" ) );
        assertExits( new File( repoDir, "org/sonatype/test/deeper/1.0.1/deeper-1.0.1.jar" ) );
        assertExits( new File( repoDir, "org/sonatype/test/empty/1.0.1/empty-1.0.1.pom" ) );
        assertExits( new File( repoDir, "org/sonatype/test/empty/1.0.1/empty-1.0.1.jar" ) );
        assertExits( new File( repoDir, "org/sonatype/test/non-java/1.0.1/non-java-1.0.1.pom" ) );
        assertExits( new File( repoDir, "org/sonatype/test/non-java/1.0.1/non-java-1.0.1.xml" ) );
        File grouper = new File( repoDir, "org/sonatype/test/grouper/1.0.1/grouper-1.0.1.pom" );
        assertExits( grouper );

        FileInputStream fis = new FileInputStream( grouper );
        Model pom = new MavenXpp3Reader().read( fis );
        fis.close();

        assertEquals( pom.getGroupId(), "org.sonatype.test" );
        assertEquals( pom.getArtifactId(), "grouper" );
        assertEquals( pom.getVersion(), "1.0.1" );
        assertEquals( pom.getPackaging(), "pom" );
        assertEquals( pom.getDependencies().size(), 3 );

        Dependency dep1 = (Dependency) pom.getDependencies().get( 0 );
        assertEquals( dep1.getGroupId(), "org.sonatype.test" );
        assertEquals( dep1.getArtifactId(), "empty" );
        assertEquals( dep1.getVersion(), "1.0.1" );
    }

    private void assertExits( File file )
    {
        if ( !file.exists() )
        {
            fail( "File " + file + " doesn't exists!" );
        }
    }

}
