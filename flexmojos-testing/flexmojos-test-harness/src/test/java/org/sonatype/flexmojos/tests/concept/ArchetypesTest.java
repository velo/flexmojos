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
import java.io.IOException;

import org.apache.maven.it.VerificationException;
import org.sonatype.flexmojos.tests.AbstractFlexMojosTests;
import org.testng.annotations.Test;

public class ArchetypesTest
    extends AbstractFlexMojosTests
{

    private static File baseTestDir;

    @Test
    public void testApplication()
        throws Exception
    {
        testArchetype( "application" );
    }

    @Test
    public void testLibrary()
        throws Exception
    {
        testArchetype( "library" );
    }

    @Test
    public void testModular()
        throws Exception
    {
        testArchetype( "modular-webapp" );
    }

    private void testArchetype( String kind )
        throws IOException, VerificationException
    {
        if ( baseTestDir == null )
        {
            baseTestDir = getProject( "/concept/archetype" );
        }

        String artifactId = "artifact-it-" + kind;

        String[] args =
            new String[] { "-DarchetypeGroupId=org.sonatype.flexmojos",
                "-DarchetypeArtifactId=flexmojos-archetypes-" + kind, "-DarchetypeVersion=" + getProperty( "version" ),
                "-DgroupId=org.sonatype.flexmojos.it", "-DartifactId=" + artifactId, "-Dversion=1.0-SNAPSHOT" };
        test( baseTestDir, "org.apache.maven.plugins:maven-archetype-plugin:2.0-alpha-4:create", args );

        File testDir2 = new File( baseTestDir, artifactId );
        test( testDir2, "install" );
    }

}
