/**
 * Copyright 2008 Marvin Herman Froeder
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
                "-DarchetypeArtifactId=flexmojos-archetypes-" + kind, "-DarchetypeVersion=" + getFlexmojosVersion(),
                "-DgroupId=org.sonatype.flexmojos.it", "-DartifactId=" + artifactId, "-Dversion=1.0-SNAPSHOT" };
        test( baseTestDir, "org.apache.maven.plugins:maven-archetype-plugin:2.0-alpha-5:create", args );

        File testDir2 = new File( baseTestDir, artifactId );
        test( testDir2, "install" );
    }

}
