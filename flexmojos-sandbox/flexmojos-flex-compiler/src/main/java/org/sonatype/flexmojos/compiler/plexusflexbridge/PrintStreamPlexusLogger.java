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
