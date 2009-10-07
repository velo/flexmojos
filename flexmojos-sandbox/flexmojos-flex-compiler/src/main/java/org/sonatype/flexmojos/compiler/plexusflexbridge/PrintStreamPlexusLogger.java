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
package org.sonatype.flexmojos.compiler.plexusflexbridge;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.codehaus.plexus.logging.Logger;

public class PrintStreamPlexusLogger
    extends PrintStream
{

    public PrintStreamPlexusLogger( Logger logger, int levelInfo )
    {
        super( getLogger( logger, levelInfo ) );
    }

    private static OutputStream getLogger( final Logger logger, final int levelInfo )
    {
        return new OutputStream()
        {
            private StringBuilder buff = new StringBuilder();

            @Override
            public void write( int b )
                throws IOException
            {
                char c = (char) b;
                if ( '\n' == c )
                {
                    switch ( levelInfo )
                    {
                        case Logger.LEVEL_INFO:
                            logger.info( buff.toString() );
                            break;
                        case Logger.LEVEL_WARN:
                            logger.warn( buff.toString() );
                            break;
                        case Logger.LEVEL_ERROR:
                            logger.error( buff.toString() );
                            break;
                        case Logger.LEVEL_FATAL:
                            logger.fatalError( buff.toString() );
                            break;
                        default:
                            logger.debug( buff.toString() );
                            break;
                    }
                    buff.delete( 0, buff.length() );
                }
                else
                {
                    buff.append( c );
                }
            }
        };
    }

}
