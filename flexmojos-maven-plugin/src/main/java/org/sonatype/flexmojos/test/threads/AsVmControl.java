package org.sonatype.flexmojos.test.threads;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.codehaus.plexus.component.annotations.Component;

/**
 * This class will ping Action Script virtual machine to make sure if the application still running
 * 
 * @author velo
 */
@Component( role = AsVmControl.class, instantiationStrategy = "per-lookup" )
public class AsVmControl
    extends AbstractSocketThread
{
    public static final String ROLE = AsVmControl.class.getName();

    public static final String STATUS = "Server Status";

    public static final String OK = "OK";

    private int testTimeout;

    @Override
    protected void handleRequest()
        throws SocketTimeoutException, SocketException, IOException
    {
        getLogger().debug( "AsVmControl handleRequest" );

        clientSocket.setSoTimeout( testTimeout );

        int errorCount = 0;

        while ( true )
        {
            getLogger().debug( "get status" );
            BufferedWriter out = new BufferedWriter( new OutputStreamWriter( super.out ) );
            out.write( STATUS );
            out.flush();

            try
            {
                getLogger().debug( "got status" );
                BufferedReader in = new BufferedReader( new InputStreamReader( super.in ) );
                String result = in.readLine();
                getLogger().debug( "status: " + result );
                if ( !OK.equals( result ) )
                {
                    errorCount++;
                    if ( errorCount >= 3 )
                    {
                        setError( "Invalid virtual machine status: " + result, null );
                    }
                }
                else
                {
                    errorCount = 0;
                }
            }
            catch ( SocketTimeoutException e )
            {
                errorCount++;
                if ( errorCount >= 3 )
                {
                    setError( "Remote virtual machine didn't reply, looks to be stucked", e );
                }
            }

            clientSocket.setSoTimeout( 0 );
        }
    }

    public void init( int testControlPort, int firstConnectionTimeout, int testTimeout )
    {
        super.init( testControlPort );
        super.firstConnectionTimeout = firstConnectionTimeout;
        this.testTimeout = testTimeout;
    }

}
