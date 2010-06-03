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

import static org.testng.Assert.fail;

import org.codehaus.plexus.context.Context;
import org.sonatype.flexmojos.test.TestRequest;
import org.sonatype.flexmojos.test.ThreadStatus;
import org.testng.annotations.Test;

public class InvalidPlayerTest
    extends AbstractAsVmLauncherTest
{

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );

        context.put( "flashplayer.command", "invalid_flash_player" );
    }

    @Test( timeOut = 20000, enabled = false )
    public void invalidPlayer()
        throws Exception
    {
        TestRequest request = new TestRequest();
        request.setSwf( VALID_SWF.getSwf() );
        request.setFlashplayerCommand( "invalid_flash_player" );

        if ( launcher.useXvfb() )
        {
            launcher.start( request );

            do
            {
                Thread.yield();
                Thread.sleep( 100 );
            }
            while ( !ThreadStatus.ERROR.equals( launcher.getStatus() ) );

            System.out.println( launcher.getConsoleOutput() );
        }
        else
        {
            try
            {
                launcher.start( request );

                fail( launcher.getConsoleOutput() );
            }
            catch ( LaunchFlashPlayerException e )
            {
                // expected
            }
            finally
            {
                launcher.stop();
            }
        }
    }

}
