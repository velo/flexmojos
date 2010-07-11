/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
