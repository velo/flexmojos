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
