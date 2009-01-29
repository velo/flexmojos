package org.sonatype.flexmojos.tests.coverage;

import org.testng.annotations.Test;

public class SourceFileResolverTest
    extends AbstractCoverageTest
{
    @Test
    public void testSourceFileResolver()
        throws Exception
    {
        standardCoverageTester( "source-file-resolver" );
    }

}
