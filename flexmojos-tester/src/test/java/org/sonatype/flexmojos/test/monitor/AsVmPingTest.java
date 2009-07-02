package org.sonatype.flexmojos.test.monitor;

import static org.testng.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.codehaus.plexus.PlexusTestNGCase;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.flexmojos.test.ThreadStatus;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AsVmPingTest
    extends PlexusTestNGCase
{

    private AsVmPing ping;

    private int port;

    @BeforeMethod
    public void setUp()
        throws Exception
    {
        ping = lookup( AsVmPing.class );

        ServerSocket ss = new ServerSocket( 0 );
        port = ss.getLocalPort();
        ss.close();
    }

    @AfterMethod
    public void tearDown()
        throws Exception
    {
        ping.stop();
    }

    @Test( timeOut = 10000 )
    public void checkConnectTimeout()
        throws Exception
    {
        ping.start( port, 5000, 1000 );

        do
        {
            Thread.yield();
            Thread.sleep( 100 );
        }
        while ( !ThreadStatus.ERROR.equals( ping.getStatus() ) );

        assertEquals( ping.getError().getClass(), SocketTimeoutException.class );
    }

    @Test( timeOut = 20000, invocationCount = 10 )
    public void checkPing()
        throws Exception
    {
        ping.start( port, 500000, 100000 );

        do
        {
            Thread.yield();
            Thread.sleep( 100 );
        }
        while ( !ThreadStatus.STARTED.equals( ping.getStatus() ) );

        Socket s = new Socket( "localhost", port );
        BufferedReader in = new BufferedReader( new InputStreamReader( s.getInputStream() ) );
        OutputStream out = s.getOutputStream();

        String request = in.readLine();
        assertEquals( request, AsVmPing.STATUS );
        IOUtil.copy( AsVmPing.OK, out );
        IOUtil.copy( AsVmPing.EOL, out );

        request = in.readLine();
        assertEquals( request, AsVmPing.STATUS );
        IOUtil.copy( AsVmPing.OK, out );
        IOUtil.copy( AsVmPing.EOL, out );

        request = in.readLine();
        assertEquals( request, AsVmPing.STATUS );
        IOUtil.copy( AsVmPing.OK, out );
        IOUtil.copy( AsVmPing.EOL, out );

        request = in.readLine();
        assertEquals( request, AsVmPing.STATUS );
        IOUtil.copy( AsVmPing.FINISHED, out );
        IOUtil.copy( AsVmPing.EOL, out );

        do
        {
            Thread.yield();
            Thread.sleep( 100 );
        }
        while ( !ThreadStatus.DONE.equals( ping.getStatus() ) );

        s.close();
    }

    @Test( timeOut = 20000 )
    public void checkPingTimeout()
        throws Exception
    {
        ping.start( port, 500000, 1000 );

        do
        {
            Thread.yield();
            Thread.sleep( 100 );
        }
        while ( !ThreadStatus.STARTED.equals( ping.getStatus() ) );

        Socket s = new Socket( "localhost", port );
        BufferedReader in = new BufferedReader( new InputStreamReader( s.getInputStream() ) );

        String request = in.readLine();
        assertEquals( request, AsVmPing.STATUS );

        do
        {
            Thread.yield();
            Thread.sleep( 100 );
        }
        while ( !ThreadStatus.ERROR.equals( ping.getStatus() ) );

        assertEquals( ping.getError().getClass(), SocketTimeoutException.class );

        s.close();
    }

}
