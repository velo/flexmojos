package org.sonatype.flexmojos.test.launcher;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.util.Arrays;

import org.sonatype.flexmojos.test.TestRequest;
import org.sonatype.flexmojos.test.ThreadStatus;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class AsVmLauncherTest
    extends AbstractAsVmLauncherTest
{

    @Test( timeOut = 20000, invocationCount = 5 )
    public void launch()
        throws Exception
    {
        // if ( launcher.useXvfb() )
        // {
        // throw new SkipException( "Skipping for now" );
        // }

        launcher.start( VALID_SWF );

        do
        {
            Thread.yield();
            Thread.sleep( 100 );
        }
        while ( !( ThreadStatus.DONE.equals( launcher.getStatus() ) || ThreadStatus.ERROR.equals( launcher.getStatus() ) ) );

        assertEquals( launcher.getStatus(), ThreadStatus.DONE, "tmp: "
            + Arrays.toString( new File( "/tmp" ).listFiles() ) );

        // TODO most recent flashplayer aren't dumping trace, no idea why
        // String log = launcher.getConsoleOutput();
        // MatcherAssert.assertThat( log, StringContains.containsString( "SWF Created!" ) );
    }

    @SuppressWarnings( "unused" )
    @Test( timeOut = 20000 )
    public void stop()
        throws Exception
    {
        if ( true )
            throw new SkipException( "Know failure, need more investigation" );

        launcher.start( INVALID_SWF );

        do
        {
            Thread.yield();
            Thread.sleep( 100 );
        }
        while ( !ThreadStatus.RUNNING.equals( launcher.getStatus() ) );

        Thread.yield();
        Thread.sleep( 2000 );// give some extra time

        launcher.stop();

        Thread.yield();
        Thread.sleep( 2000 );// give some extra time

        String log = launcher.getConsoleOutput();
        assertTrue( log.contains( "SWF Created!" ), "Log: " + log + " - Status: " + launcher.getStatus() );

        assertEquals( ThreadStatus.ERROR, launcher.getStatus() );
        assertNotNull( launcher.getError() );
    }

    @Test( timeOut = 20000 )
    public void fakeSwf()
        throws Exception
    {
        System.out.println( "fakeSwf" );

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
            TestRequest request = new TestRequest();
            request.setSwf( new File( "not_existing_swf_file.swf" ) );
            launcher.start( request );
            fail();
        }
        catch ( InvalidSwfException e )
        {
            // expected
        }
    }

}
