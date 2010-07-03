package org.sonatype.flexmojos.util;

import java.io.IOException;
import java.net.ServerSocket;

public class SocketUtil
{
    public static int freePort()
        throws IOException
    {
        ServerSocket ss = new ServerSocket( 0 );
        try
        {
            return ss.getLocalPort();
        }
        finally
        {
            ss.close();
        }
    }
}
