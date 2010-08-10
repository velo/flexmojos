package org.sonatype.flexmojos.fbtests.flashbuilder;

import java.io.File;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sonatype.flexmojos.fbtests.AbstractFlexMojosFbTest;

@RunWith( SWTBotJunit4ClassRunner.class )
public class FlashBuilderTest extends AbstractFlexMojosFbTest
{
	@Test
    public void testHelloWorld()
        throws Exception
    {
    	String projectName = "hello-world";
    	
    	String dir = test( getProject( "flashbuilder/intro/"+projectName ), "flexmojos:flashbuilder" ).getBasedir();
    	
    	assertProjectFiles( dir, ProjectType.FLEX );
    	
        importAndBuildProject( dir );
        
        File bin = new File( dir, "bin-debug" );
        File swf = new File( bin, "main.swf" );
        Assert.assertTrue( "Main SWF should have been built.", swf.exists() );
        Long swfKb = swf.length()/1000;
        Assert.assertEquals( 598, swfKb.intValue() );
        
        // Check HTML template does not exist
        File template = new File( dir, "html-template" );
        Assert.assertTrue( "Html Template should not exist.", !template.exists() );
        
        // Check config
        assertGeneralProjectConfig( projectName, 4.0, false );
    }
}
