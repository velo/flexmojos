package org.sonatype.flexmojos.test.monitor;

import static org.sonatype.flexmojos.test.monitor.CommConstraints.EOL;
import static org.sonatype.flexmojos.test.monitor.CommConstraints.FINISHED;
import static org.sonatype.flexmojos.test.monitor.CommConstraints.OK;
import static org.sonatype.flexmojos.test.monitor.CommConstraints.STATUS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.flexmojos.test.ThreadStatus;

/**
 * This class will ping Action Script virtual machine to make sure if the application still running
 * 
 * @author velo
 */
@Component( role = AsVmPing.class, instantiationStrategy = "per-lookup" )
public class AsVmPing
    extends AbstractSocketThread
{

    @Configuration( value = "13540" )
    private int testControlPort;

    @Configuration( value = "20000" )
    private int firstConnectionTimeout;

    @Configuration( value = "5000" )
    private int testTimeout;

    @Override
    protected void handleRequest()
        throws SocketTimeoutException, SocketException, IOException
    {
        getLogger().debug( "[CONTROL] AsVmControl handleRequest" );

        clientSocket.setSoTimeout( testTimeout );

        int errorCount = 0;

        while ( true )
        {
            getLogger().debug( "[CONTROL] query status" );
            IOUtil.copy( STATUS + EOL, out );

            try
            {
                getLogger().debug( "[CONTROL] received status" );
                BufferedReader in = new BufferedReader( new InputStreamReader( super.in ) );
                String result = in.readLine();
                getLogger().debug( "[CONTROL] status is: " + result );
                if ( !OK.equals( result ) && !FINISHED.equals( result ) )
                {
                    errorCount++;
                    if ( errorCount >= 3 )
                    {
                        status = ThreadStatus.ERROR;
                        error = new Error( "Invalid virtual machine status: " + result );

                        return;
                    }
                }
                else if ( FINISHED.equals( result ) )
                {
                    getLogger().debug( "[CONTROL] FINISHED received, terminating the thread" );
                    return;
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
                    status = ThreadStatus.ERROR;
                    error = e;

                    return;
                }
            }
            catch ( SocketException e )
            {
                if ( !e.getMessage().contains( "Broken pipe" ) )
                {
                    throw e;
                }
            }
        }
    }

    public void start()
    {
        reset();

        launch();
    }

    @Override
    protected int getTestPort()
    {
        return testControlPort;
    }

    @Override
    protected int getFirstConnectionTimeout()
    {
        return firstConnectionTimeout;
    }

}
