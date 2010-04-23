/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.test.launcher;

import java.io.File;
import java.net.URISyntaxException;

import org.codehaus.plexus.PlexusTestNGCase;
import org.codehaus.plexus.context.Context;
import org.sonatype.flexmojos.test.TestRequest;
import org.sonatype.flexmojos.test.util.OSUtils;
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
            VALID_SWF = new TestRequest();
            VALID_SWF.setSwf( new File( AsVmLauncherTest.class.getResource( "/SelftExit.swf" ).toURI() ) );
            INVALID_SWF = new TestRequest();
            INVALID_SWF.setSwf( new File( AsVmLauncherTest.class.getResource( "/NonExit.swf" ).toURI() ) );
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

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );

        context.put( "flashplayer.command", OSUtils.getPlatformDefaultCommand() );
    }

}