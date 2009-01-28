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
        test( baseTestDir, "archetype:create", args );

        File testDir2 = new File( baseTestDir, artifactId );
        test( testDir2, "install" );
    }

}
