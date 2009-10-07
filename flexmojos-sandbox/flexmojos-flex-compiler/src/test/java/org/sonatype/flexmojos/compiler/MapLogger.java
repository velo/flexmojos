/**
 * 
 */
package org.sonatype.flexmojos.compiler;

import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;

public class MapLogger
    extends AbstractLogger
{

    private final Map<String, Throwable> logs;

    public MapLogger()
    {
        super( Logger.LEVEL_DEBUG, "TestLog" );
        this.logs = new LinkedHashMap<String, Throwable>();
    }

    public void debug( String message, Throwable throwable )
    {
        logs.put( message, throwable );
    }

    public void error( String message, Throwable throwable )
    {
        logs.put( message, throwable );
    }

    public void fatalError( String message, Throwable throwable )
    {
        logs.put( message, throwable );
    }

    public Logger getChildLogger( String name )
    {
        return this;
    }

    public Map<String, Throwable> getLogs()
    {
        return logs;
    }

    public void info( String message, Throwable throwable )
    {
        logs.put( message, throwable );
    }

    public void warn( String message, Throwable throwable )
    {
        logs.put( message, throwable );
    }

}
