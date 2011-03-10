package org.sonatype.flexmojos.tests.issues;

import java.io.File;

import org.hamcrest.MatcherAssert;
import org.sonatype.flexmojos.matcher.file.FileMatcher;
import org.sonatype.flexmojos.test.FMVerifier;
import org.testng.annotations.Test;

public class Flexmojos248Test
    extends AbstractIssueTest
{

    @Test
    public void moduleFiles()
        throws Exception
    {
        FMVerifier v = testIssue( "flexmojos-248", "-DloadExternsOnModules=true" );
        String dir = v.getBasedir();
        validateCompilation( dir, v );
    }

    @Test
    public void moduleFilesLoadExternsOnModules()
        throws Exception
    {
        FMVerifier v = testIssue( "flexmojos-248", "-DloadExternsOnModules=false" );
        String dir = v.getBasedir();
        validateCompilation( dir, v );
    }

    @Test
    public void multiplePoms()
        throws Exception
    {
        File testDir = getProject( "/issues/flexmojos-248", "pom.xml", "m.xml", "p1.xml", "p2.xml" );
        FMVerifier v = test( testDir, "install", "-f", "m.xml" );
        String dir = v.getBasedir();
        test( testDir, "install", "-f", "p1.xml" );
        test( testDir, "install", "-f", "p2.xml" );
        validateCompilation( dir, v );
    }

    private void validateCompilation( String dir, FMVerifier v )
        throws Exception
    {
        File target = new File( dir, "target" );
        File main = new File( target, "test-flex-modules-0.0.1-SNAPSHOT.swf" );
        MatcherAssert.assertThat( main, FileMatcher.isFile() );

        File module1 = new File( target, "test-flex-modules-0.0.1-SNAPSHOT-Module1.swf" );
        MatcherAssert.assertThat( module1, FileMatcher.isFile() );
        File module2 = new File( target, "test-flex-modules-0.0.1-SNAPSHOT-Module2.swf" );
        MatcherAssert.assertThat( module2, FileMatcher.isFile() );

        assertSeftExit( main, 3539, v );
    }
}
