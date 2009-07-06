package org.sonatype.flexmojos.test.launcher;

import static org.testng.Assert.fail;

import org.codehaus.plexus.context.Context;
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

    @Test( timeOut = 20000 )
    public void invalidPlayer()
        throws Exception
    {
        set( launcher, "flashplayerCommand", "invalid_flash_player" );

        if ( launcher.useXvfb() )
        {
            launcher.start( VALID_SWF );

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
                launcher.start( VALID_SWF );

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
