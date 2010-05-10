package org.sonatype.flexmojos.tests.issues;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Test;

public class Flexmojos220Test
    extends AbstractIssueTest
{

    @Test
    public void localizedModules()
        throws Exception
    {
        String baseDir = testIssue( "flexmojos-220" ).getBasedir();
        File main = new File( baseDir, "target/flexmojos-220-1.0-SNAPSHOT.swf" );
        Assert.assertTrue( main.exists() );
        File module = new File( baseDir, "target/flexmojos-220-1.0-SNAPSHOT-module.swf" );
        Assert.assertTrue( module.exists() );
        File locale = new File( baseDir, "target/locales/flexmojos-220-1.0-SNAPSHOT-en_US.swf" );
        Assert.assertTrue( locale.exists() );
        File report = new File( baseDir, "target/flexmojos-220-1.0-SNAPSHOT-link-report.xml" );
        Assert.assertTrue( report.exists() );
    }

}
