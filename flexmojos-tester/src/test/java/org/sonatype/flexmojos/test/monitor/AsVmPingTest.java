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
