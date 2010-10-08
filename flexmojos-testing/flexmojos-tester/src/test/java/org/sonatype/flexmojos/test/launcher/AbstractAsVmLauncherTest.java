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
package org.sonatype.flexmojos.test.launcher;

import static org.sonatype.flexmojos.util.PathUtil.file;
import static org.sonatype.flexmojos.util.PathUtil.path;

import java.io.File;
import java.net.URISyntaxException;

import org.codehaus.plexus.PlexusTestNGCase;
import org.codehaus.plexus.context.Context;
import org.sonatype.flexmojos.test.TestRequest;
import org.sonatype.flexmojos.util.OSUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class AbstractAsVmLauncherTest
    extends PlexusTestNGCase
{

    protected AsVmLauncher launcher;

    protected static final TestRequest VALID_SWF;

    protected static final TestRequest INVALID_SWF;

    static
    {
        try
        {
            String fp;
            if ( OSUtils.isWindows() )
            {
                fp = path( file( "target/flashplayer/flashplayer.exe" ) );
            }
            else if ( OSUtils.isLinux() )
            {
                fp = path( file( "target/flashplayer/flashplayer-linux.uexe" ) );
            }
            else
            {
                fp = path( file( "target/flashplayer/flashplayer-mac.uexe" ) );
            }

            if ( !OSUtils.isWindows() )
            {
                file( fp ).setExecutable( true );
            }

            VALID_SWF = new TestRequest();
            VALID_SWF.setSwf( new File( AsVmLauncherTest.class.getResource( "/SelftExit.swf" ).toURI() ) );
            VALID_SWF.setFlashplayerCommand( fp );
            INVALID_SWF = new TestRequest();
            INVALID_SWF.setSwf( new File( AsVmLauncherTest.class.getResource( "/NonExit.swf" ).toURI() ) );
            INVALID_SWF.setFlashplayerCommand( fp );
            INVALID_SWF.setFirstConnectionTimeout( 1000 );
            INVALID_SWF.setTestTimeout( 1000 );
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeException();// won't happen, I hope =D
        }
    }

    @BeforeMethod
    public void setUp()
        throws Exception
    {
        launcher = lookup( AsVmLauncher.class );
    }

    @AfterMethod
    public void tearDown()
        throws Exception
    {
        launcher.stop();
    }

}