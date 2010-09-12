package org.sonatype.flexmojos.generator;

public interface GeneratorLogger
{
    void debug( String message );

    void debug( String message, Throwable throwable );

    boolean isDebugEnabled();

    void info( String message );

    void info( String message, Throwable throwable );

    boolean isInfoEnabled();

    void warn( String message );

    void warn( String message, Throwable throwable );

    boolean isWarnEnabled();

    void error( String message );

    void error( String message, Throwable throwable );
}
