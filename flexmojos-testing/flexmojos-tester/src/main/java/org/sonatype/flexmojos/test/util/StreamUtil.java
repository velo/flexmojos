package org.sonatype.flexmojos.test.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.Arrays;

import org.codehaus.plexus.util.StringOutputStream;

public class StreamUtil
{

    public static String readAvailable( InputStream in )
        throws IOException
    {
        StringOutputStream out = new StringOutputStream();
        in.skip( 0 );

        int available = in.available();

        byte[] buffer = new byte[available];
        try
        {
            in.read( buffer );
        }
        catch ( SocketException e )
        {
            System.out.println( available );
            System.out.println( Arrays.toString( buffer ) );
        }
        out.write( buffer );

        out.flush();
        out.close();
        return out.toString();
    }

}
