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
        request.setFlashplayerCommand( new String[] { "invalid_flash_player" } );

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
