package org.sonatype.flexmojos.test.monitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.sonatype.flexmojos.test.AbstractControlledThread;
import org.sonatype.flexmojos.test.ControlledThread;
import org.sonatype.flexmojos.test.ThreadStatus;

public abstract class AbstractSocketThread
    extends AbstractControlledThread
    implements ControlledThread
{

    protected ServerSocket serverSocket;

    protected Socket clientSocket;

    protected InputStream in;

    protected OutputStream out;

    protected int testPort;

    protected int firstConnectionTimeout;

    public AbstractSocketThread()
    {
        super();
    }

    public void run()
    {
        try
        {
            openServerSocket();
            status = ThreadStatus.STARTED;

            openClientSocket();

            handleRequest();

            if ( !ThreadStatus.ERROR.equals( status ) )
            {
                status = ThreadStatus.DONE;
            }
        }
        catch ( SocketTimeoutException e )
        {
            status = ThreadStatus.ERROR;
            error = e;
        }
        catch ( IOException e )
        {
            status = ThreadStatus.ERROR;
            error = e;
        }
        finally
        {
            // always stop the server loop
            closeClientSocket();
            closeServerSocket();
        }
    }

    protected abstract void handleRequest()
        throws SocketTimeoutException, SocketException, IOException;

    private void openServerSocket()
        throws IOException
    {
        serverSocket = new ServerSocket( testPort );
        serverSocket.setSoTimeout( firstConnectionTimeout );

        getLogger().debug( "[" + this.getClass().getName() + "] opened server socket on port " + testPort );
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
        throws SocketException, IOException, SocketTimeoutException
    {
        // This method blocks until a connection is made.
        clientSocket = serverSocket.accept();

        // serverSocket.setSoTimeout( 0 );

        getLogger().debug( "[" + this.getClass().getName() + "] accepting data from client" );

        status = ThreadStatus.RUNNING;

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

    public void stop()
    {
        try
        {
            if ( this.serverSocket != null )
            {
                this.serverSocket.close();
            }
        }
        catch ( IOException e )
        {
            getLogger().debug( e.getMessage(), e );
        }
    }

}