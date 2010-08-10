package org.sonatype.flexmojos.fbtests.flexbuilder;

import java.io.File;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sonatype.flexmojos.fbtests.AbstractFlexMojosFbTest;

@RunWith( SWTBotJunit4ClassRunner.class )
public class FlexBuilderTest extends AbstractFlexMojosFbTest
{
	
    @Test
    public void testHelloWorld()
        throws Exception
    {
    	String projectName = "hello-world";
    	
    	String dir = test( getProject( "flexbuilder/intro/"+projectName ), "flexmojos:flexbuilder" ).getBasedir();
    	
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
	public void testFlex35SwfServiceTest()
	{
		Assert.assertEquals( true, true );
	}
    
    @Test
	public void testSimpleFlexCaching()
		throws Exception
	{
	
		String projectName = "simple-flex-caching";
		
		String dir = test( getProject( "flexbuilder/concept/"+projectName ), "clean" ).getBasedir();
		
		assertProjectFiles( dir, ProjectType.FLEX );
		
	    importAndBuildProject( dir );
	    
	    File bin = new File( dir, "bin-debug" );
	    File swf = new File( bin, "main.swf" );
	    
	    // Check SWF created correctly
	    Assert.assertTrue( "Main SWF should have been built.", swf.exists() );
	    Long swfKb = swf.length()/1000;
	    Assert.assertEquals( 100, swfKb.intValue() );
	    
	    // Check config
	    assertGeneralProjectConfig( projectName, 3.2, false );
	    
	}
   
    @Test
	public void testSimpleFlexRsl()
		throws Exception
	{
	
		String projectName = "simple-flex-rsl";
		String appProjectName = "simple-flex-rsl-application";
		String libProjectName = "simple-flex-rsl-library";
		
		test( getProject( "flexbuilder/concept/"+projectName ), "install" );
		String dir = test( getProject( "flexbuilder/concept/"+projectName ), "clean" ).getBasedir();
		
		String applicationDir = dir+"/application";
		String libraryDir = dir+"/library";
		
		assertProjectFiles( applicationDir, ProjectType.FLEX );
		assertProjectFiles( libraryDir, ProjectType.FLEX_LIBRARY );
		
	    importAndBuildProject( libraryDir );
	    importAndBuildProject( applicationDir );
	    
	    
	    File bin = new File( applicationDir, "bin-debug" );
	    File swf = new File( bin, "main.swf" );
	    
	    // Check SWF created correctly
	    Assert.assertTrue( "Main SWF should have been built.", swf.exists() );
	    Long swfKb = swf.length()/1000;
	    Assert.assertEquals( 180, swfKb.intValue() );
	    
	    // Check config
	    assertGeneralProjectConfig( appProjectName, 3.2, ProjectType.FLEX, false );
	    assertGeneralProjectConfig( libProjectName, 3.2, ProjectType.FLEX_LIBRARY, false );
	    
	}
    
    @Test
    public void testSwfWithHtmlTemplate()
    	throws Exception
    {
    	String projectName = "swf-with-htmltemplate";
    	
    	String dir = test( getProject( "flexbuilder/concept/"+projectName ), "clean" ).getBasedir();
    	
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
    
}
