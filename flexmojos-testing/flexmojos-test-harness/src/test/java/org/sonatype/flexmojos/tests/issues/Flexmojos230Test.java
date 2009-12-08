package org.sonatype.flexmojos.tests.issues;

import java.io.File;

import junit.framework.Assert;

import org.testng.annotations.Test;

public class Flexmojos230Test
    extends AbstractIssueTest
{

    @Test
    public void regularAsdoc()
        throws Exception
    {
        String baseDir = testIssue( "flexmojos-230", "-Dasdoc.aggregate=false" ).getBasedir();
        File asdoc = new File( baseDir, "target/asdoc" );
        Assert.assertFalse( asdoc.exists() );

        File moduleA = new File( baseDir, "moduleA/target/asdoc" );
        Assert.assertTrue( moduleA.exists() );

        File moduleB = new File( baseDir, "moduleB/target/asdoc" );
        Assert.assertTrue( moduleB.exists() );

        File aClass = new File( moduleA, "AClass.html" );
        Assert.assertTrue( aClass.exists() );

        File bClass = new File( moduleB, "BClass.html" );
        Assert.assertTrue( bClass.exists() );

    }

    @Test
    public void aggregatedAsdoc()
        throws Exception
    {
        String baseDir = testIssue( "flexmojos-230", "-Dasdoc.aggregate=true" ).getBasedir();
        File target = new File( baseDir, "target" );
        Assert.assertTrue( target.exists() );

        File aClass = new File( target, "asdoc/AClass.html" );
        Assert.assertTrue( aClass.exists() );

        File bClass = new File( target, "asdoc/BClass.html" );
        Assert.assertTrue( bClass.exists() );

        File moduleA = new File( baseDir, "moduleA/target/asdoc" );
        Assert.assertFalse( moduleA.exists() );

        File moduleB = new File( baseDir, "moduleB/target/asdoc" );
        Assert.assertFalse( moduleB.exists() );
    }

}
