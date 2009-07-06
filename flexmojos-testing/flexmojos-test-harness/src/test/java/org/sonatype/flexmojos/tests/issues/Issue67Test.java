package org.sonatype.flexmojos.tests.issues;

import java.io.File;

import org.testng.annotations.Test;

public class Issue67Test
    extends AbstractIssueTest
{

    @Test
    public void issue67()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0067" );
        test( testDir, "org.sonatype.flexmojos:flexmojos-maven-plugin:" + getProperty( "version" ) + ":asdoc" );
    }

}
