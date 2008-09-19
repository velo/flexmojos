package info.flexmojos.tests;

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
        test(getProject("intro/hello-world"), "install");
    }
    @Test
    public void helloWordNoInherit()
    throws Exception  {
        test(getProject("intro/hello-world-no-inherit"), "install");
    }

}
