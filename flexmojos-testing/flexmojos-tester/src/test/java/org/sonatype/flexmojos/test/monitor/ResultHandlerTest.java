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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.sonatype.flexmojos.test.monitor.CommConstraints.END_OF_TEST_RUN;
import static org.sonatype.flexmojos.test.monitor.CommConstraints.END_OF_TEST_SUITE;
import static org.sonatype.flexmojos.test.monitor.CommConstraints.NULL_BYTE;
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

    private static final String REPORT2 = "Another cool report!" + END_OF_TEST_SUITE;

    private static final String REPORT1 = "Awesome complex report!" + END_OF_TEST_SUITE;

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

        set( result, "testReportPort", port );
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
        result.start();

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
        result.start();

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
        IOUtil.copy( String.valueOf( NULL_BYTE ), out );

        Thread.yield();
        Thread.sleep( 100 );

        assertEquals( result.getTestReportData().size(), 1 );

        IOUtil.copy( REPORT2, out );
        IOUtil.copy( String.valueOf( NULL_BYTE ), out );

        Thread.yield();
        Thread.sleep( 100 );

        assertEquals( result.getTestReportData().size(), 2 );

        IOUtil.copy( END_OF_TEST_RUN, out );
        IOUtil.copy( String.valueOf( NULL_BYTE ), out );

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
