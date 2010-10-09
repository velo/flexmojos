package org.sonatype.flexmojos.tests.issues;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;

import org.sonatype.flexmojos.matcher.file.FileMatcher;
import org.testng.annotations.Test;

public class Issue0039Test
    extends AbstractIssueTest
{

    @Test
    public void issue39()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0039" );
        test( testDir, "flexmojos:asdoc" );

        assertThat( new File( testDir, "target/asdoc/main.html" ), FileMatcher.exists() );
    }

}
