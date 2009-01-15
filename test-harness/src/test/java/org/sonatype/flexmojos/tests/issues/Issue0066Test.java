package org.sonatype.flexmojos.tests.issues;

import static org.testng.AssertJUnit.assertTrue;

import java.io.File;

import org.testng.annotations.Test;

public class Issue0066Test
    extends AbstractIssueTest
{

    @Test( timeOut = 120000, groups = { "generator" } )
    public void issue66()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0066" );
        test( testDir, "install" );

        // Issue 62 test
        File another = new File( testDir, "flex/src/main/flex/org/sonatype/flexmojos/generator/AnotherPojo.as" );
        assertTrue( "File not found " + another, another.isFile() );

        // Issue 65 test
        File pojo = new File( testDir, "flex/src/main/flex/org/sonatype/flexmojos/generator/SimplePojo.as" );
        assertTrue( "File not found " + pojo, pojo.isFile() );
        File base =
            new File( testDir,
                      "flex/target/generated-sources/flex-mojos/org/sonatype/flexmojos/generator/SimplePojoBase.as" );
        assertTrue( "File not found " + base, base.isFile() );
    }

}
