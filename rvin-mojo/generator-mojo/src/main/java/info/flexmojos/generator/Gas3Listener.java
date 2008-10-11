package info.flexmojos.generator;

import org.apache.maven.plugin.logging.Log;
import org.granite.generator.Input;
import org.granite.generator.Listener;
import org.granite.generator.Output;

/**
 * Logging <code>GenerationListener</code>.
 * 
 * @author Juraj Burian
 */
public class Gas3Listener
    implements Listener
{

    private final Log log;

    /**
     * @param log
     */
    public Gas3Listener( final Log log )
    {
        this.log = log;
    }

    public void error( String message )
    {
        error( message, null );
    }

    public void error( String message, Throwable e )
    {
        log.error( message, e );
    }

    public void info( String message )
    {
        info( message, null );
    }

    public void info( String message, Throwable e )
    {
        log.info( message, e );
    }

    public void warn( String message )
    {
        warn( message, null );
    }

    public void warn( String message, Throwable e )
    {
        log.warn( message, e );
    }

    public void generating(Input<?> input, Output<?> output) {
        info( "  Generating: " + output.getDescription() );
        
    }

    public void skipping(Input<?> input, Output<?> output) {
        info( "  Skipping: " + output.getDescription() );
        
    }

}
