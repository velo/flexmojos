package org.sonatype.flexmojos.tests.coverage;

import java.io.File;

import org.sonatype.flexmojos.tests.AbstractFlexMojosTests;

public class AbstractCoverageTest
    extends AbstractFlexMojosTests
{

    public AbstractCoverageTest()
    {
        super();
    }

    public void standardCoverageTester( String coverageName )
        throws Exception
    {
        File testDir = getProject( "/coverage/" + coverageName );
        test( testDir, "install" );
    }

}