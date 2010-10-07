package org.sonatype.flexmojos.tests.concept;

import java.io.File;

import org.sonatype.flexmojos.test.FMVerifier;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ManifestTest
    extends AbstractConceptTest
{

    @Test
    public void manifestCreation()
        throws Exception
    {
        FMVerifier v = standardConceptTester( "manifest" );
        File dir = new File( v.getBasedir() );
        Assert.assertTrue( new File( dir, "target/manifest.xml" ).isFile() );
    }

}
