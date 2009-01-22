package org.sonatype.flexmojos.tests.issues;

import static org.testng.AssertJUnit.assertTrue;

import java.io.File;

import org.sonatype.flexmojos.tests.AbstractFlexMojosTests;
import org.testng.annotations.Test;

public class Issue0017Test
    extends AbstractFlexMojosTests
{
    @Test( timeOut = 120000 )
    public void issue17()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0017" );
        test( testDir, "site", "-o" );

        File asdoc = new File( testDir, "target/site/asdoc" );
        assertTrue( "asdoc directory must exist", asdoc.isDirectory() );
    }

}
