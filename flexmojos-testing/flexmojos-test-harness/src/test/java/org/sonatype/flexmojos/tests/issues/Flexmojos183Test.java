package org.sonatype.flexmojos.tests.issues;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;

import org.sonatype.flexmojos.matcher.file.FileMatcher;
import org.sonatype.flexmojos.test.FMVerifier;
import org.testng.annotations.Test;

public class Flexmojos183Test
    extends AbstractIssueTest
{

    @Test( enabled = false )
    public void font2swf()
        throws Exception
    {
        FMVerifier v = testIssue( "flexmojos-183", "-DisIt=true" );
        String dir = v.getBasedir();

        File target = new File( dir, "target" );
        File main = new File( target, "flexmojos-183-1.0-SNAPSHOT.swf" );

        assertSeftExit( main, 3539, v );

        File font = new File( target, "classes/myFont.swf" );
        assertThat( font, FileMatcher.exists() );
    }
}
