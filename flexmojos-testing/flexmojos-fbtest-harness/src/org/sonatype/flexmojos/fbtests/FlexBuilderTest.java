package org.sonatype.flexmojos.fbtests;

import java.io.File;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( SWTBotJunit4ClassRunner.class )
public class FlexBuilderTest extends AbstractFlexMojosFbTest
{

    @Test
    public void testHelloWorld()
        throws Exception
    {
    	String projectName = "hello-world";
    	
    	String dir = test( getProject( "intro/"+projectName ), "flexmojos:flexbuilder" ).getBasedir();
    	
    	assertProjectFiles( dir, ProjectType.FLEX );
    	
        importAndBuildProject( dir );
        
        File bin = new File( dir, "bin-debug" );
        File swf = new File( bin, "main.swf" );
        Assert.assertTrue( "Main SWF should have been built.", swf.exists() );
        Long swfKb = swf.length()/1000;
        Assert.assertEquals( 282, swfKb.intValue() );
        
        // Check HTML template does not exist
        File template = new File( dir, "html-template" );
        Assert.assertTrue( "Html Template should not exist.", !template.exists() );
        
        // Check config
        assertGeneralProjectConfig( projectName, 3.2, false );
    }
    
    @Test
    public void testSwfWithHtmlTemplate()
    	throws Exception
    {
    	String projectName = "swf-with-htmltemplate";
    	
    	String dir = test( getProject( "intro/"+projectName ), "clean" ).getBasedir();
    	
    	assertProjectFiles( dir, ProjectType.FLEX );
    	
        importAndBuildProject( dir );
        
        File bin = new File( dir, "bin-debug" );
        File swf = new File( bin, "main.swf" );
        File template = new File( dir, "html-template" );
        
        // Check SWF created correctly
        Assert.assertTrue( "Main SWF should have been built.", swf.exists() );
        Long swfKb = swf.length()/1000;
        Assert.assertEquals( 282, swfKb.intValue() );
        
        // Check HTML template was generated
        Assert.assertTrue( "Html Template should have been built.", template.exists() );
        
        // Check config
        assertGeneralProjectConfig( projectName, 3.2, true );
    }

    @AfterClass
    public static void sleep()
    {
       AbstractFlexMojosFbTest.bot.sleep( 2000 );
    }

}
