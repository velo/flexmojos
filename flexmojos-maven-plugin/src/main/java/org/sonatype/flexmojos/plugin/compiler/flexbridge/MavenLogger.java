package org.sonatype.flexmojos.plugin.compiler.flexbridge;

import org.apache.maven.plugin.logging.Log;

import flex2.tools.oem.Logger;
import flex2.tools.oem.Message;

public class MavenLogger
    implements Logger
{

    private Log log;

    public MavenLogger( Log log )
    {
        this.log = log;
    }

    public void log( Message message, int errorCode, String source )
    {
        if ( Message.ERROR.equals( message.getLevel() ) )
        {
            log.error( getMessage( message, source ) );
        }
        else if ( Message.INFO.equals( message.getLevel() ) )
        {
            log.info( getMessage( message, source ) );
        }
        else if ( Message.WARNING.equals( message.getLevel() ) )
        {
            log.warn( getMessage( message, source ) );
        }
        else
        {
            log.info( getMessage( message, source ) );
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
