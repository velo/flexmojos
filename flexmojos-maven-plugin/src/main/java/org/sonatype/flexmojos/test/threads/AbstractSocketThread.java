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
package org.sonatype.flexmojos.test.threads;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.codehaus.plexus.logging.AbstractLogEnabled;

public abstract class AbstractSocketThread
    extends AbstractLogEnabled
    implements ControlledThread
{

    protected ServerSocket serverSocket = null;

    protected Socket clientSocket = null;

    protected InputStream in = null;

    protected OutputStream out = null;

    private Error error;

    private ThreadStatus status;

    private boolean holdStatus;

    protected Integer testPort;

    protected Integer firstConnectionTimeout;

    public AbstractSocketThread()
    {
        super();
    }

    protected void init( int portNumber )
    {
        this.testPort = portNumber;
    }

    public void run()
    {
        if ( testPort == null )
        {
            setError( "Test port not defined!", null );
        }
        try
        {
            openServerSocket();
            openClientSocket();

            handleRequest();

            setStatus( ThreadStatus.DONE );
        }
        catch ( SocketTimeoutException e )
        {
            setError( "Timeout waiting for flexunit report", e );
        }
        catch ( IOException e )
        {
            setError( "Error receiving report from flexunit", e );
        }
        finally
        {
            // always stop the server loop
            closeClientSocket();
            closeServerSocket();
        }
    }

    public void setStatus( ThreadStatus status )
    {
        if ( !holdStatus )
        {
            this.status = status;
        }
    }

    public void setError( Error error )
    {
        this.error = error;
    }

    protected abstract void handleRequest()
        throws SocketTimeoutException, SocketException, IOException;

    protected void setError( String msg, Exception root )
    {
        getLogger().debug( msg, root );

        setStatus( ThreadStatus.ERROR );
        error = new Error( msg + " - " + getClass(), root );
        throw error;
    }

    private void openServerSocket()
        throws IOException
    {
        serverSocket = new ServerSocket( testPort );
        if ( firstConnectionTimeout != null )
        {
            serverSocket.setSoTimeout( firstConnectionTimeout );
        }

        getLogger().debug( "["+this.getClass().getName()+"] opened server socket on port "+testPort );
    }

    private void closeServerSocket()
    {
        if ( serverSocket != null )
        {
            try
            {
                serverSocket.close();
            }
            catch ( IOException e )
            {
                // ignore
            }
        }
    }

    protected void openClientSocket()
        throws SocketException, IOException
    {
        // This method blocks until a connection is made.
        try
        {
            clientSocket = serverSocket.accept();
        }
        catch ( SocketTimeoutException e )
        {
            setError( "Flash player didn't open connection.", e );
        }
        // serverSocket.setSoTimeout( 0 );

        getLogger().debug( "["+this.getClass().getName()+"] accepting data from client" );

        setStatus( ThreadStatus.RUNNING );

        in = clientSocket.getInputStream();
        out = clientSocket.getOutputStream();
    }
    
    protected void closeClientSocket()
    {
        // Close the output stream.
        if ( out != null )
        {
            try
            {
                out.close();
            }
            catch ( IOException e )
            {
                // ignore
            }
        }

        // Close the input stream.
        if ( in != null )
        {
            try
            {
                in.close();
            }
            catch ( IOException e )
            {
                // ignore
            }
        }

        // Close the client socket.
        if ( clientSocket != null )
        {
            try
            {
                clientSocket.close();
            }
            catch ( IOException e )
            {
                // ignore
            }
        }
    }

    public Error getError()
    {
        return this.error;
    }

    public ThreadStatus getStatus()
    {
        return this.status;
    }

    public void stop()
    {
        this.holdStatus = true;
        try
        {
            this.serverSocket.close();
        }
        catch ( IOException e )
        {
            getLogger().debug( e.getMessage(), e );
        }
    }

}