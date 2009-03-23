package org.sonatype.flexmojos.test.threads;

import java.io.IOException;
import java.net.SocketException;
import java.text.MessageFormat;

/**
 * Create a server socket for receiving the test reports from FlexUnit. We read the test reports inside of a Thread.
 * 
 * @plexus.component role="org.sonatype.flexmojos.test.threads.ResultHandler"
 */
public class ResultHandler
    extends AbstractSocketThread
    implements ControlledThread
{
    public static final String ROLE = ResultHandler.class.getName();

    public static final String END_OF_TEST_RUN = "<endOfTestRun/>";

    public static final String END_OF_TEST_SUITE = "</testsuite>";

    public static final char NULL_BYTE = '\u0000';

    public static final String POLICY_FILE_REQUEST = "<policy-file-request/>";

    public final static String DOMAIN_POLICY =
        "<cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"{0}\" /></cross-domain-policy>";

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
                // getLogger().debug( "Recivied data: " + data );
                buffer = new StringBuffer();

                if ( data.equals( POLICY_FILE_REQUEST ) )
                {
                    getLogger().debug( "Send policy file" );

                    sendPolicyFile();
                    closeClientSocket();
                    openClientSocket();
                }
                else if ( data.endsWith( END_OF_TEST_SUITE ) )
                {
                    getLogger().debug( "End test suite" );

                    this.testReportData.add( data );
                }
                else if ( data.equals( END_OF_TEST_RUN ) )
                {
                    getLogger().debug( "End test run" );
                    break;
                }
            }
            else
            {
                buffer.append( chr );
            }
        }

        getLogger().debug( "Socket buffer " + buffer );
    }

    protected void sendPolicyFile()
        throws IOException
    {
        out.write( MessageFormat.format( DOMAIN_POLICY, new Object[] { Integer.toString( testPort ) } ).getBytes() );

        out.write( NULL_BYTE );

        getLogger().debug( "sent policy file" );
    }
}
