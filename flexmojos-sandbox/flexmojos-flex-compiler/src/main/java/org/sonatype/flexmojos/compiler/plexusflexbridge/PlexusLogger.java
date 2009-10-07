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
