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
package org.sonatype.flexmojos.test.monitor;

import static org.sonatype.flexmojos.test.monitor.CommConstraints.EOL;
import static org.sonatype.flexmojos.test.monitor.CommConstraints.FINISHED;
import static org.sonatype.flexmojos.test.monitor.CommConstraints.OK;
import static org.sonatype.flexmojos.test.monitor.CommConstraints.STATUS;
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
    private int firstConnectionTimeout;
    private int testTimeout;

    @BeforeMethod
    public void setUp()
        throws Exception
    {
        ping = lookup( AsVmPing.class );

        ServerSocket ss = new ServerSocket( 0 );
        port = ss.getLocalPort();
        ss.close();

        this.firstConnectionTimeout = 5000;
        this.testTimeout = 1000;
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
        ping.start(port, firstConnectionTimeout, testTimeout);

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
        set( ping, "firstConnectionTimeout", 500000 );
        set( ping, "testTimeout", 100000 );

        ping.start(port, firstConnectionTimeout, testTimeout);

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
        assertEquals( request, STATUS );
        IOUtil.copy( OK + EOL, out );

        request = in.readLine();
        assertEquals( request, STATUS );
        IOUtil.copy( OK + EOL, out );

        request = in.readLine();
        assertEquals( request, STATUS );
        IOUtil.copy( OK + EOL, out );

        request = in.readLine();
        assertEquals( request, STATUS );
        IOUtil.copy( FINISHED + EOL, out );

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
        set( ping, "firstConnectionTimeout", 500000 );

        ping.start(port, firstConnectionTimeout, testTimeout);

        do
        {
            Thread.yield();
            Thread.sleep( 100 );
        }
        while ( !ThreadStatus.STARTED.equals( ping.getStatus() ) );

        Socket s = new Socket( "localhost", port );
        BufferedReader in = new BufferedReader( new InputStreamReader( s.getInputStream() ) );

        String request = in.readLine();
        assertEquals( request, STATUS );

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
