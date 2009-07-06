package org.sonatype.flexmojos.tests.issues;

import java.io.File;

import org.testng.annotations.Test;

public class Flexmojos130Test
    extends AbstractIssueTest
{

    @Test
    public void generateDita()
        throws Exception
    {
        File testDir = getProject( "/issues/flexmojos-130" );
        test( testDir, "org.sonatype.flexmojos:flexmojos-maven-plugin:" + getProperty( "version" ) + ":dita-asdoc" );
    }

    @Test
    public void attachDita()
        throws Exception
    {
        File testDir = getProject( "/issues/flexmojos-130" );
        test( testDir, "install" );
    }
}
