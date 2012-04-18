/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.flexmojos.oss.compiler.command;

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
