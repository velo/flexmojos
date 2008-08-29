package info.flexmojos.tests;

import static junit.framework.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class IT0091HelloWordTest
    extends AbstractFlexMojosTests
{

    @Test
    public void helloWordTest()
        throws Exception  {
//        File testDir = getProject( "/issues/issue-0017" );
//        test( testDir, "site" );
//
//        File asdoc = new File( testDir, "target/asdoc" );
//        assertTrue( "asdoc directory must exist", asdoc.isDirectory() );
        test(getProject("simple-flex-application"), "install");
    }

}
