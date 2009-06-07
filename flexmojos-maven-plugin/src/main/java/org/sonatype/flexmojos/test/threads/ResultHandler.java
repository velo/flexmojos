package org.sonatype.flexmojos.test.threads;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import org.codehaus.plexus.component.annotations.Component;

/**
 * Create a server socket for receiving the test reports from FlexUnit. We read the test reports inside of a Thread.
 */
@Component( role = ResultHandler.class, instantiationStrategy = "per-lookup" )
public class ResultHandler
    extends AbstractSocketThread
    implements ControlledThread
{
    public static final String ROLE = ResultHandler.class.getName();

    public static final String END_OF_TEST_RUN = "<endOfTestRun/>";

    public static final String END_OF_TEST_SUITE = "</testsuite>";

    public static final String ACK_OF_TEST_RESULT = "<endOfTestRunAck/>";
    
    public static final char NULL_BYTE = '\u0000';

    protected List<String> testReportData;

    public List<String> getTestReportData()
    {
        return testReportData;
    }

    protected void handleRequest()
        throws SocketException, IOException
    {
        StringBuffer buffer = new StringBuffer();
        int bite = -1;

        while ( ( bite = in.read() ) != -1 )
        {
            final char chr = (char) bite;

            if ( chr == NULL_BYTE )
            {
                final String data = buffer.toString();
                getLogger().debug( "[RESULT] Recivied data: " + data  );
                buffer = new StringBuffer();

                if ( data.endsWith( END_OF_TEST_SUITE ) )
                {
                    getLogger().debug( "[RESULT] End test suite" );

                    this.testReportData.add( data );
                }
                else if ( data.equals( END_OF_TEST_RUN ) )
                {
                    getLogger().debug( "[RESULT] End test run - sending ACK: "+ACK_OF_TEST_RESULT );
               
                    //Sending the acknowledgement to testrunner
                    
                    BufferedWriter out = new BufferedWriter( new OutputStreamWriter( super.out ) );
                    out.write( ACK_OF_TEST_RESULT + NULL_BYTE );
                    out.flush();
                    break;                    
                }
            }
            else
            {
                buffer.append( chr );
            }
        }

        getLogger().debug( "[RESULT] Socket buffer " + buffer );
    }

    @Override
    public void init( int portNumber )
    {
        super.init( portNumber );

        testReportData = new ArrayList<String>();
    }
}
