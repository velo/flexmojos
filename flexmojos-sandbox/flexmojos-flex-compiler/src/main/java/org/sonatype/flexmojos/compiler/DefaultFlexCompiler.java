package org.sonatype.flexmojos.compiler;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.flexmojos.compiler.command.Command;
import org.sonatype.flexmojos.compiler.command.Result;
import org.sonatype.flexmojos.compiler.util.FlexCompilerArgumentParser;

import flex2.compiler.util.ThreadLocalToolkit;
import flex2.tools.ASDoc;
import flex2.tools.Compc;
import flex2.tools.DigestTool;
import flex2.tools.Mxmlc;
import flex2.tools.Optimizer;

@Component( role = FlexCompiler.class )
public class DefaultFlexCompiler
    extends AbstractLogEnabled
    implements FlexCompiler
{

    @Requirement
    private FlexCompilerArgumentParser parser;

    public int compileSwc( final ICompcConfiguration configuration )
        throws Exception
    {
        return execute( new Command()
        {
            public void command()
                throws Exception
            {
                String[] args = parser.parseArguments( configuration, ICompcConfiguration.class );
                logArgs( args );
                Compc.compc( args );
            }
        } );
    }

    public int compileSwf( MxmlcConfigurationHolder cfgHolder )
        throws Exception
    {
        final List<String> argsList =
            parser.getArgumentsList( cfgHolder.configuration, ICommandLineConfiguration.class );
        if ( cfgHolder.sourceFile != null )
        {
            argsList.add( cfgHolder.sourceFile.getAbsolutePath() );
        }
        return execute( new Command()
        {
            public void command()
            {
                String[] args = argsList.toArray( new String[argsList.size()] );
                logArgs( args );
                Mxmlc.mxmlc( args );
            }
        } );
    }

    public int asdoc( final IASDocConfiguration configuration )
        throws Exception
    {
        return execute( new Command()
        {
            public void command()
            {
                String[] args = parser.parseArguments( configuration, IASDocConfiguration.class );
                logArgs( args );
                ASDoc.asdoc( args );
            }
        } );
    }

    public int digest( final IDigestConfiguration configuration )
        throws Exception
    {
        return execute( new Command()
        {
            public void command()
                throws Exception
            {
                String[] args = parser.parseArguments( configuration, IDigestConfiguration.class );
                logArgs( args );
                DigestTool.main( args );
            }
        } );
    }

    public int optimize( final IOptimizerConfiguration configuration )
        throws Exception
    {
        return execute( new Command()
        {
            public void command()
                throws Exception
            {
                String[] args = parser.parseArguments( configuration, IOptimizerConfiguration.class );
                logArgs( args );
                Optimizer.main( args );
            }
        } );
    }

    private int execute( final Command command )
        throws Exception
    {
        final Result r = new Result();
        Thread t = new Thread( new Runnable()
        {
            public void run()
            {
                SecurityManager sm = System.getSecurityManager();

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

                try
                {
                    command.command();
                }
                catch ( Exception e )
                {
                    r.setException( e );
                }
                finally
                {
                    System.setSecurityManager( sm );
                }

                r.setExitCode( ThreadLocalToolkit.errorCount() );
            }
        } );
        t.setUncaughtExceptionHandler( new UncaughtExceptionHandler()
        {
            public void uncaughtException( Thread t, Throwable e )
            {
                if ( e instanceof Exception )
                {
                    r.setException( (Exception) e );
                }
                else
                {
                    r.setException( new Exception( e ) );
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

        if ( r.getException() != null )
        {
            throw r.getException();
        }

        return r.getExitCode();
    }

    private void logArgs( String[] args )
    {
        if ( getLogger().isDebugEnabled() )
        {
            StringBuilder sb = new StringBuilder();
            for ( String arg : args )
            {
                if ( arg.startsWith( "-" ) )
                {
                    sb.append( '\n' );
                }
                sb.append( arg );
                sb.append( ' ' );
            }
            getLogger().debug( "Compilation arguments:" + sb );
        }
    }

}
