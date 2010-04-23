/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.compiler;

import static org.sonatype.flexmojos.compiler.util.ParseArguments.getArguments;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

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

    public void compileSwf( ICommandLineConfiguration configuration, File sourceFile )
    {
        List<String> args = getArguments( configuration, ICommandLineConfiguration.class );
        args.add( sourceFile.getAbsolutePath() );
        execute( new MxmlcCommand( args ) );
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
