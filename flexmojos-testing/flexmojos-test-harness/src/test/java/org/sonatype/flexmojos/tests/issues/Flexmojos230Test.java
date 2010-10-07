package org.sonatype.flexmojos.tests.issues;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import java.io.File;

import org.sonatype.flexmojos.matcher.file.FileMatcher;
import org.testng.annotations.Test;

public class Flexmojos230Test
    extends AbstractIssueTest
{

    @Test
    public void regularAsdoc()
        throws Exception
    {
        String baseDir = testIssue( "flexmojos-230", "-Dflex.asdoc.aggregate=false" ).getBasedir();
        File asdoc = new File( baseDir, "target/asdoc" );
        assertThat( asdoc, not( FileMatcher.exists() ) );

        File moduleA = new File( baseDir, "moduleA/target/asdoc" );
        assertThat( moduleA, FileMatcher.exists() );

        File moduleB = new File( baseDir, "moduleB/target/asdoc" );
        assertThat( moduleB, FileMatcher.exists() );

        File aClass = new File( moduleA, "AClass.html" );
        assertThat( aClass, FileMatcher.exists() );

        File bClass = new File( moduleB, "BClass.html" );
        assertThat( bClass, FileMatcher.exists() );

    }

    @Test
    public void aggregatedAsdoc()
        throws Exception
    {
        String baseDir = testIssue( "flexmojos-230", "-Dflex.asdoc.aggregate=true" ).getBasedir();
        File target = new File( baseDir, "target" );
        assertThat( target, FileMatcher.exists() );

        File aClass = new File( target, "asdoc/AClass.html" );
        assertThat( aClass, FileMatcher.exists() );

        File bClass = new File( target, "asdoc/BClass.html" );
        assertThat( bClass, FileMatcher.exists() );

        File moduleA = new File( baseDir, "moduleA/target/asdoc" );
        assertThat( moduleA, not( FileMatcher.exists() ) );

        File moduleB = new File( baseDir, "moduleB/target/asdoc" );
        assertThat( moduleB, not( FileMatcher.exists() ) );
    }

}
