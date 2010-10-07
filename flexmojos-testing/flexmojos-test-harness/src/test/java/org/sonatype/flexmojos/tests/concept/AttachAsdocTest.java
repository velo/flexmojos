package org.sonatype.flexmojos.tests.concept;

import java.io.File;

import org.sonatype.flexmojos.test.FMVerifier;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AttachAsdocTest
    extends AbstractConceptTest
{

    @Test
    public void attachAsdoc()
        throws Exception
    {
        FMVerifier v = standardConceptTester( "attach-asdoc" );
        File target = new File( v.getBasedir(), "target" );

        Assert.assertTrue( target.exists() );

        File doc = new File( target, "attach-asdoc-1.0-SNAPSHOT-asdoc.zip" );
        Assert.assertTrue( doc.exists() );
    }
}
