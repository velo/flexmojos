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
                Compc.compc( parser.parseArguments( configuration, ICompcConfiguration.class ) );
            }
        } );
    }

    public int compileSwf( MxmlcConfigurationHolder cfgHolder )
        throws Exception
    {
        final List<String> args = parser.getArgumentsList( cfgHolder.configuration, ICommandLineConfiguration.class );
        if ( cfgHolder.sourceFile != null )
        {
            args.add( cfgHolder.sourceFile.getAbsolutePath() );
        }
        return execute( new Command()
        {
            public void command()
            {
                Mxmlc.mxmlc( args.toArray( new String[args.size()] ) );
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
                ASDoc.asdoc( parser.parseArguments( configuration, IASDocConfiguration.class ) );
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
                DigestTool.main( parser.parseArguments( configuration, IDigestConfiguration.class ) );
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
                Optimizer.main( parser.parseArguments( configuration, IOptimizerConfiguration.class ) );
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

}
