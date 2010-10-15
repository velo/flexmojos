package org.sonatype.flexmojos.tests.issues;

import org.sonatype.flexmojos.test.FMVerifier;
import org.sonatype.flexmojos.tests.AbstractFlexMojosTests;
import org.testng.annotations.Test;

public class Flexmojos355Test
    extends AbstractFlexMojosTests
{

    @Test
    public void flexmojos355()
        throws Exception
    {
        FMVerifier v = test( getProject( "intro/hello-world" ), "install", "-Dflex.defaultBackgroundColor=#FFFFFF" );
        v.verifyTextInLog( "defaultBackgroundColor = 16777215" );
        v.verifyTextInLog( "-default-background-color=16777215" );
    }

}
