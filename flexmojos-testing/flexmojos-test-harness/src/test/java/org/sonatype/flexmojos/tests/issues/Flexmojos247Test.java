package org.sonatype.flexmojos.tests.issues;

import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.text.StringContains;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Flexmojos247Test
    extends AbstractIssueTest
{

    @Test
    public void includeAsClasses()
        throws Exception
    {
        String baseDir = testIssue( "flexmojos-247" ).getBasedir();
        File target = new File( baseDir, "target" );
        Assert.assertTrue( target.exists() );

        File swc = new File( target, "flexmojos-247-1.0-SNAPSHOT.swc" );
        Assert.assertTrue( swc.exists() );

        String catalog;
        ZipFile zf = new ZipFile( swc );
        try
        {
            InputStream in = zf.getInputStream( zf.getEntry( "catalog.xml" ) );
            catalog = IOUtils.toString( in );
            in.close();
        }
        finally
        {
            zf.close();
        }

        // must have both classes and the uri
        MatcherAssert.assertThat( catalog, StringContains.containsString( "AClass" ) );
        MatcherAssert.assertThat( catalog, StringContains.containsString( "BClass" ) );
        MatcherAssert.assertThat( catalog, StringContains.containsString( "http://flexmojos.sonatype.org/tests" ) );
    }
}
