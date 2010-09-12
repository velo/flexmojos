package org.sonatype.flexmojos.generator;

import org.apache.maven.plugin.logging.Log;

public class MavenGeneratorLogger
    implements GeneratorLogger
{

    private Log log;

    public MavenGeneratorLogger( Log log )
    {
        this.log = log;
    }

    public void debug( String message )
    {
        this.log.debug( message );
    }

    public void debug( String message, Throwable throwable )
    {
        this.log.debug( message, throwable );
    }

    public boolean isDebugEnabled()
    {
        return this.isDebugEnabled();
    }

    public void info( String message )
    {
        this.log.info( message );
    }

    public void info( String message, Throwable throwable )
    {
        this.log.info( message, throwable );
    }

    public boolean isInfoEnabled()
    {
        return this.log.isInfoEnabled();
    }

    public void warn( String message )
    {
        this.log.warn( message );
    }

    public void warn( String message, Throwable throwable )
    {
        this.log.warn( message, throwable );
    }

    public boolean isWarnEnabled()
    {
        return this.log.isWarnEnabled();
    }

    public void error( String message )
    {
        this.log.error( message );
    }

    public void error( String message, Throwable throwable )
    {
        this.log.error( message, throwable );
    }

}
