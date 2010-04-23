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
