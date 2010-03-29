package flex2.tools.oem.internal;

import flex2.compiler.ILocalizableMessage;
import flex2.compiler.util.AbstractLogger;
import flex2.compiler.util.ThreadLocalToolkit;
import flex2.tools.oem.Logger;

public class OEMLogAdapter
    extends AbstractLogger
{
    private Logger oemLogger;

    private int errorCount;

    private int warningCount;

    public OEMLogAdapter( Logger l )
    {
        init( ThreadLocalToolkit.getLocalizationManager() );
        setLogger( l );
    }

    public void setLogger( Logger l )
    {
        this.oemLogger = l;
    }

    public int errorCount()
    {
        return this.errorCount;
    }

    public void includedFileAffected( String path )
    {
    }

    public void includedFileUpdated( String path )
    {
    }

    public void log( ILocalizableMessage m )
    {
        log( m, null );
    }

    public void log( ILocalizableMessage m, String source )
    {
        if ( "warning".equals( m.getLevel() ) )
        {
            this.warningCount += 1;
        }
        else if ( "error".equals( m.getLevel() ) )
        {
            this.errorCount += 1;
        }

        if ( this.oemLogger == null )
            return;
        this.oemLogger.log( m, -1, null );
    }

    public void logDebug( String debug )
    {
    }

    public void logDebug( String path, String debug )
    {
    }

    public void logDebug( String path, int line, String debug )
    {
    }

    public void logDebug( String path, int line, int col, String debug )
    {
    }

    public void logError( String error )
    {
        logError( null, -1, -1, error, null, -1 );
    }

    public void logError( String path, String error )
    {
        logError( null, -1, -1, error, null, -1 );
    }

    public void logError( String path, String error, int errorCode )
    {
        logError( path, -1, -1, error, null, errorCode );
    }

    public void logError( String path, int line, String error )
    {
        logError( path, line, -1, error, null, -1 );
    }

    public void logError( String path, int line, String error, int errorCode )
    {
        logError( path, line, -1, error, null, errorCode );
    }

    public void logError( String path, int line, int col, String error )
    {
        logError( path, line, col, error, null, -1 );
    }

    public void logError( String path, int line, int col, String error, String source )
    {
        logError( path, line, col, error, source, -1 );
    }

    public void logError( String path, int line, int col, String error, String source, int errorCode )
    {
        this.errorCount += 1;
        if ( this.oemLogger == null )
            return;
        this.oemLogger.log( new GenericMessage( "error", path, line, col, error ), errorCode, source );
    }

    public void logInfo( String info )
    {
        logInfo( null, -1, -1, info );
    }

    public void logInfo( String path, String info )
    {
        logInfo( path, -1, -1, info );
    }

    public void logInfo( String path, int line, String info )
    {
        logInfo( path, line, -1, info );
    }

    public void logInfo( String path, int line, int col, String info )
    {
        if ( this.oemLogger == null )
            return;
        this.oemLogger.log( new GenericMessage( "info", path, line, col, info ), -1, null );
    }

    public void logWarning( String warning )
    {
        logWarning( null, -1, -1, warning, null, -1 );
    }

    public void logWarning( String path, String warning )
    {
        logWarning( path, -1, -1, warning, null, -1 );
    }

    public void logWarning( String path, String warning, int errorCode )
    {
        logWarning( path, -1, -1, warning, null, errorCode );
    }

    public void logWarning( String path, int line, String warning )
    {
        logWarning( path, line, -1, warning, null, -1 );
    }

    public void logWarning( String path, int line, String warning, int errorCode )
    {
        logWarning( path, line, -1, warning, null, errorCode );
    }

    public void logWarning( String path, int line, int col, String warning )
    {
        logWarning( path, line, col, warning, null, -1 );
    }

    public void logWarning( String path, int line, int col, String warning, String source )
    {
        logWarning( path, line, col, warning, source, -1 );
    }

    public void logWarning( String path, int line, int col, String warning, String source, int errorCode )
    {
        this.warningCount += 1;
        if ( this.oemLogger == null )
            return;
        this.oemLogger.log( new GenericMessage( "warning", path, line, col, warning ), errorCode, source );
    }

    public void needsCompilation( String path, String reason )
    {
    }

    public int warningCount()
    {
        return this.warningCount;
    }
}
