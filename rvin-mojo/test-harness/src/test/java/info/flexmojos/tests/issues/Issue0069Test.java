/*
 * Flex-mojos is a set of maven plugins to allow maven users to compile, optimize, test and ... Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008 Marvin Herman Froeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.flexmojos.tests.issues;

import static org.testng.AssertJUnit.assertTrue;
import info.rvin.flexmojos.utilities.MavenUtils;

import java.io.File;
import java.io.FileReader;
import java.util.Locale;

import org.codehaus.plexus.util.IOUtil;
import org.testng.annotations.Test;

public class Issue0069Test
    extends AbstractIssueTest
{

    @Test( timeOut = 120000 )
    public void issue69()
        throws Exception
    {
        // Microsoft is unbelievable, Application Data folder name is localized
        if ( MavenUtils.isWindows() )
        {
            if ( !Locale.getDefault().equals( Locale.ENGLISH ) )
            {
                return;
            }
        }

        final String[] trusts =
            new String[] { "AppData/Roaming/Macromedia/Flash Player/#Security/FlashPlayerTrust",
                "Application Data/Macromedia/Flash Player/#Security/FlashPlayerTrust",
                ".macromedia/Flash_Player/#Security/FlashPlayerTrust",
                "Library/Preferences/Macromedia/Flash Player/#Security/FlashPlayerTrust" };

        File userHome = new File( System.getProperty( "user.home" ) );

        File mavenCfg = null;
        for ( String folder : trusts )
        {
            File fpTrustFolder = new File( userHome, folder );
            if ( fpTrustFolder.exists() && fpTrustFolder.isDirectory() )
            {
                mavenCfg = new File( fpTrustFolder, "maven.cfg" );
                if ( mavenCfg.exists() )
                {
                    mavenCfg.delete();
                }
                break;
            }
        }

        testIssue( "issue-0069" );

        File testDir = getProject( "/issues/issue-0069" );
        File swf = new File( testDir, "target/test-classes/TestRunner.swf" );

        assertTrue( "Flex-mojos should generate maven.cfg: " + mavenCfg.getAbsolutePath(), mavenCfg.exists() );

        String cfg = IOUtil.toString( new FileReader( mavenCfg ) );

        assertTrue( "Flex-mojos should write trust localtion", cfg.contains( swf.getAbsolutePath() ) );
    }

}
