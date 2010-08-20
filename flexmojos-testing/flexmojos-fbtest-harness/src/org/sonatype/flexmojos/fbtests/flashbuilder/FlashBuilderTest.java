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
	
	@Test
    public void testIssueFlexmojos343()
        throws Exception
    {
    	String projectName = "flexmojos-343";
    	
    	String dir = test( getProject( "flashbuilder/issues/"+projectName ), "flexmojos:flashbuilder" ).getBasedir();
    	
    	assertProjectFiles( dir, ProjectType.FLEX );
    	
        importAndBuildProject( dir );
        
        File bin = new File( dir, "bin-debug" );
        File swf = new File( bin, "main.swf" );
        Assert.assertTrue( "Main SWF should have been built.", swf.exists() );
        Long swfKb = swf.length()/1000;
        Assert.assertEquals( 607, swfKb.intValue() );
        
        // Check HTML template does not exist
        File template = new File( dir, "html-template" );
        Assert.assertTrue( "Html Template should not exist.", !template.exists() );
        
        // Check config
        assertGeneralProjectConfig( projectName, 4.1, false );
    }
}
