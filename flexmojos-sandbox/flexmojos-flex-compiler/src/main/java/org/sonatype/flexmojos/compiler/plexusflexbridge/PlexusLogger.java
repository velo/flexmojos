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

import flex2.tools.oem.Logger;
import flex2.tools.oem.Message;

public class PlexusLogger
    implements Logger
{

    private org.codehaus.plexus.logging.Logger logger;

    public PlexusLogger( org.codehaus.plexus.logging.Logger logger )
    {
        this.logger = logger;
    }

    public void log( Message message, int errorCode, String source )
    {
        if ( Message.ERROR.equals( message.getLevel() ) )
        {
            logger.error( getMessage( message, source ) );
        }
        else if ( Message.INFO.equals( message.getLevel() ) )
        {
            logger.info( getMessage( message, source ) );
        }
        else if ( Message.WARNING.equals( message.getLevel() ) )
        {
            logger.warn( getMessage( message, source ) );
        }
        else
        {
            logger.info( getMessage( message, source ) );
        }
    }

    private String getMessage( Message msg, String source )
    {
        StringBuilder sb = new StringBuilder();

        if ( msg.getPath() != null )
        {
            sb.append( msg.getPath() );
            sb.append( ':' );
            sb.append( '[' );
            sb.append( msg.getLine() );
            sb.append( ',' );
            sb.append( msg.getColumn() );
            sb.append( ']' );
            sb.append( ' ' );
        }

        sb.append( msg.toString() );

        if ( source != null )
        {
            sb.append( source );
        }

        return sb.toString();
    }
}
