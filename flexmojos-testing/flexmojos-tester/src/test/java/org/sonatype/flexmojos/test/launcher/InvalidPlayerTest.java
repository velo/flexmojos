package org.sonatype.flexmojos.test.launcher;

import static org.testng.Assert.fail;

import org.codehaus.plexus.context.Context;
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
