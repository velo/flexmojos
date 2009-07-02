package org.sonatype.flexmojos.test.launcher;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;

import org.sonatype.flexmojos.test.ThreadStatus;
import org.testng.annotations.Test;

public class AsVmLauncherTest
    extends AbstractAsVmLauncherTest
{

    @Test( timeOut = 20000 )
    public void launch()
        throws Exception
    {
        launcher.start( VALID_SWF );

        do
        {
            Thread.yield();
            Thread.sleep( 100 );
        }
        while ( !ThreadStatus.DONE.equals( launcher.getStatus() ) );

        String log = launcher.getConsoleOutput();
        assertTrue( log.contains( "SWF Created!" ) );
    }

    @Test( timeOut = 20000 )
    public void stop()
        throws Exception
    {
        launcher.start( INVALID_SWF );

        do
        {
            Thread.yield();
            Thread.sleep( 100 );
        }
        while ( !ThreadStatus.RUNNING.equals( launcher.getStatus() ) );

        Thread.yield();
        Thread.sleep( 2000 );// give some extra time

        String log = launcher.getConsoleOutput();
        assertTrue( log.contains( "SWF Created!" ), "Log: " + log + " - Status: " + launcher.getStatus() );

        launcher.stop();

        Thread.yield();
        Thread.sleep( 2000 );// give some extra time

        assertEquals( ThreadStatus.ERROR, launcher.getStatus() );
        assertNotNull( launcher.getError() );
    }

    @Test( timeOut = 20000 )
    public void fakeSwf()
        throws Exception
    {
        try
        {
            launcher.start( null );
            fail();
        }
        catch ( InvalidSwfException e )
        {
            // expected
        }

        try
        {
            launcher.start( new File( "not_existing_swf_file.swf" ) );
            fail();
        }
        catch ( InvalidSwfException e )
        {
            // expected
        }
    }

}
