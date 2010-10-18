package org.sonatype.flexmojos.tests.concept;

import java.io.File;

import org.sonatype.flexmojos.test.FMVerifier;
import org.testng.annotations.Test;

public class ConfiguratorTest
    extends AbstractConceptTest
{

    @Test
    public void configurator()
        throws Exception
    {
        File testDir = getProject( "/concept/configurator" );
        FMVerifier v = test( testDir, "flexmojos:configurator" );
        v.verifyTextInLog( "Running configurator for a SWF project" );
        v.verifyTextInLog( "configurator-1.0-SNAPSHOT.swf" );
    }

}
