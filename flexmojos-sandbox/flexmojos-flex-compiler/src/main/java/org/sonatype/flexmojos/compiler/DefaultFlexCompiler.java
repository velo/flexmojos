package org.sonatype.flexmojos.compiler;

import static org.sonatype.flexmojos.compiler.util.ParseArguments.getArguments;

import java.io.PrintStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.flexmojos.compiler.command.Command;
import org.sonatype.flexmojos.compiler.command.CompcCommand;
import org.sonatype.flexmojos.compiler.command.MxmlcCommand;
import org.sonatype.flexmojos.compiler.plexusflexbridge.PlexusLogger;
import org.sonatype.flexmojos.compiler.plexusflexbridge.PlexusPathResolve;
import org.sonatype.flexmojos.compiler.plexusflexbridge.PrintStreamPlexusLogger;

import flex2.tools.oem.internal.OEMLogAdapter;

@Component( role = FlexCompiler.class )
public class DefaultFlexCompiler
    extends AbstractLogEnabled
    implements FlexCompiler, Initializable
{

    private ClassLoader classLoader;

    public void initialize()
        throws InitializationException
    {
        CompilerClassLoader cl = new CompilerClassLoader( getClass().getClassLoader() );
        try
        {
            cl.loadAPI();
        }
        catch ( ClassNotFoundException e )
        {
            throw new InitializationException( e.getMessage(), e );
        }

        this.classLoader = cl;
    }

    public void compileSwc( ICompcConfiguration configuration )
    {
        execute( new CompcCommand( getArguments( configuration, ICompcConfiguration.class ) ) );
    }

    public void compileSwf( ICommandLineConfiguration configuration )
    {
        execute( new MxmlcCommand( getArguments( configuration, ICommandLineConfiguration.class ) ) );
    }

    private void execute( final Command command )
    {
        Thread t = new Thread( new Runnable()
        {
            public void run()
            {
                SecurityManager sm = System.getSecurityManager();
                ClassLoader cl = Thread.currentThread().getContextClassLoader();

                Thread.currentThread().setContextClassLoader( classLoader );
                System.setSecurityManager( new SecurityManager()
                {
                    public void checkPermission( java.security.Permission perm )
                    {
                        if ( perm.getName().contains( "exitVM" ) )
                        {
                            throw new CompilerSecurityException();
                        }
                    }
                } );

                CompilerThreadLocal.logger.set( new OEMLogAdapter( new PlexusLogger( getLogger() ) ) );
                CompilerThreadLocal.pathResolver.set( new PlexusPathResolve() );
                PrintStream out = System.out;
                PrintStream err = System.err;
                System.setOut( new PrintStreamPlexusLogger( getLogger(), Logger.LEVEL_INFO ) );
                System.setErr( new PrintStreamPlexusLogger( getLogger(), Logger.LEVEL_ERROR ) );

                try
                {
                    command.command();
                }
                finally
                {
                    System.setSecurityManager( sm );
                    Thread.currentThread().setContextClassLoader( cl );
                    CompilerThreadLocal.logger.set( null );
                    CompilerThreadLocal.pathResolver.set( null );
                    System.setOut( err );
                    System.setOut( out );
                }
            }
        } );
        t.start();
        Thread.yield();

        try
        {
            t.join();
        }
        catch ( InterruptedException e )
        {
        }
    }

}
