package org.sonatype.flexmojos.compiler.command;

import java.lang.Thread.UncaughtExceptionHandler;

import flex2.compiler.util.ThreadLocalToolkit;

public class CommandUtil
{

    public static Result execute( final Command command, boolean sychronize )
            throws Exception
        {
            final Result r = new Result();
            Thread t = new Thread( new Runnable()
            {
                public void run()
                {
    //                SecurityManager sm = System.getSecurityManager();
    //
    //                System.setSecurityManager( new SecurityManager()
    //                {
    //                    public void checkPermission( java.security.Permission perm )
    //                    {
    //                        if ( perm.getName().contains( "exitVM" ) )
    //                        {
    //                            throw new CompilerSecurityException();
    //                        }
    //                    }
    //                } );
    
                    try
                    {
                        command.command();
                    }
    //                catch ( CompilerSecurityException e )
    //                {
    //                    // that is fine, just we preventing adobe from killing the VM
    //                }
                    catch ( Exception e )
                    {
                        r.setException( e );
                    }
    //                finally
    //                {
    //                    System.setSecurityManager( sm );
    //                }
    
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
            r.setThread( t );
    
            if ( sychronize )
            {
                Thread.yield();
                try
                {
                    t.join();
                }
                catch ( InterruptedException e )
                {
                }
            }
    
            return r;
        }

}
