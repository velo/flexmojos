package org.sonatype.flexmojos.test.monitor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.codehaus.plexus.PlexusTestNGCase;
import org.codehaus.plexus.util.IOUtil;
import org.hamcrest.collection.IsCollectionContaining;
import org.sonatype.flexmojos.test.ThreadStatus;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResultHandlerTest
    extends PlexusTestNGCase
{

    private static final String REPORT2 = "Another cool report!" + ResultHandler.END_OF_TEST_SUITE;

    private static final String REPORT1 = "Awesome complex report!" + ResultHandler.END_OF_TEST_SUITE;

    private ResultHandler result;

    private int port;

    @BeforeMethod
    public void setUp()
        throws Exception
    {
        result = lookup( ResultHandler.class );

        ServerSocket ss = new ServerSocket( 0 );
        port = ss.getLocalPort();
        ss.close();
    }

    @AfterMethod
    public void tearDown()
        throws Exception
    {
        result.stop();
    }

    @Test( timeOut = 10000 )
    public void stopNoResults()
        throws Exception
    {
        result.start( port );

        do
        {
            Thread.yield();
            Thread.sleep( 100 );
        }
        while ( !ThreadStatus.STARTED.equals( result.getStatus() ) );

        Thread.sleep( 1000 );

        result.stop();

        do
        {
            Thread.yield();
            Thread.sleep( 100 );
        }
        while ( !ThreadStatus.ERROR.equals( result.getStatus() ) );

        assertEquals( result.getError().getClass(), SocketException.class );
    }

    @Test( )
    public void sendResult()
        throws Exception
    {
        result.start( port );

        do
        {
            Thread.yield();
            Thread.sleep( 100 );
        }
        while ( !ThreadStatus.STARTED.equals( result.getStatus() ) );

        Socket s = new Socket( "localhost", port );
        BufferedReader in = new BufferedReader( new InputStreamReader( s.getInputStream() ) );
        OutputStream out = s.getOutputStream();

        IOUtil.copy( REPORT1, out );
        IOUtil.copy( String.valueOf( ResultHandler.NULL_BYTE ), out );

        Thread.yield();
        Thread.sleep( 100 );

        assertEquals( result.getTestReportData().size(), 1 );

        IOUtil.copy( REPORT2, out );
        IOUtil.copy( String.valueOf( ResultHandler.NULL_BYTE ), out );

        Thread.yield();
        Thread.sleep( 100 );

        assertEquals( result.getTestReportData().size(), 2 );

        IOUtil.copy( ResultHandler.END_OF_TEST_RUN, out );
        IOUtil.copy( String.valueOf( ResultHandler.NULL_BYTE ), out );

        Thread.yield();
        Thread.sleep( 100 );

        do
        {
            Thread.yield();
            Thread.sleep( 100 );
        }
        while ( !ThreadStatus.DONE.equals( result.getStatus() ) );

        assertEquals( result.getTestReportData().size(), 2 );
        assertThat( result.getTestReportData(), IsCollectionContaining.hasItems( REPORT1, REPORT2 ) );
    }
}
