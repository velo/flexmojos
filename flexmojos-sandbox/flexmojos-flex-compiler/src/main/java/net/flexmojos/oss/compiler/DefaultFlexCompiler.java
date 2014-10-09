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
package net.flexmojos.oss.compiler;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import net.flexmojos.oss.compiler.command.Command;
import net.flexmojos.oss.compiler.command.CommandUtil;
import net.flexmojos.oss.compiler.command.Result;
import net.flexmojos.oss.compiler.util.FlexCompilerArgumentParser;

import flex2.tools.ASDoc;
import flex2.tools.DigestTool;

@Component( role = FlexCompiler.class )
public class DefaultFlexCompiler
    extends AbstractLogEnabled
    implements FlexCompiler
{

    @Requirement
    private FlexCompilerArgumentParser parser;

    public Result compileSwc( final ICompcConfiguration configuration, boolean sychronize )
        throws Exception
    {
        return CommandUtil.execute( new Command()
        {
            public void command()
                    throws Exception
            {
                String[] args = parser.parseArguments( configuration, ICompcConfiguration.class );
                logArgs( args );
                try {
                    executeCompcMain(args);
                } catch (Throwable t) {
                    throw new Exception("Exception during Compc execution", t);
                }
            }
        }, sychronize );
    }

    public Result compileSwf( MxmlcConfigurationHolder cfgHolder, boolean sychronize )
        throws Exception
    {
        final List<String> argsList =
            parser.getArgumentsList( cfgHolder.configuration, ICommandLineConfiguration.class );
        if ( cfgHolder.sourceFile != null )
        {
            argsList.add( cfgHolder.sourceFile.getAbsolutePath() );
        }
        return CommandUtil.execute( new Command()
        {
            public void command()
                    throws Exception
            {
                String[] args = argsList.toArray( new String[argsList.size()] );
                logArgs( args );
                try {
                    executeMxmlcMain(args);
                } catch (Throwable t) {
                    throw new Exception("Exception during Mxmlc execution", t);
                }
            }
        }, sychronize );
    }

    public Result asdoc( final IASDocConfiguration configuration, boolean sychronize )
        throws Exception
    {
        return CommandUtil.execute( new Command()
        {
            public void command()
            {
                String[] args = parser.parseArguments( configuration, IASDocConfiguration.class );
                logArgs( args );
                // Force the XML Transformer to the Xalan version that comes with Flex
                String defaultTransfomer = System.getProperty( "javax.xml.transform.TransformerFactory" );
                System.setProperty( "javax.xml.transform.TransformerFactory",
                                    "org.apache.xalan.processor.TransformerFactoryImpl" );
                ASDoc.asdoc( args );
                // and set it back to the default
                if ( defaultTransfomer == null )
                {
                    System.getProperties().remove( "javax.xml.transform.TransformerFactory" );
                }
                else
                {
                    System.setProperty( "javax.xml.transform.TransformerFactory", defaultTransfomer );
                }
            }
        }, sychronize );
    }

    public Result digest( final IDigestConfiguration configuration, boolean sychronize )
        throws Exception
    {
        return CommandUtil.execute( new Command()
        {
            public void command()
                throws Exception
            {
                String[] args = parser.parseArguments( configuration, IDigestConfiguration.class );
                logArgs( args );
                Method m = DigestTool.class.getDeclaredMethod( "digestTool", String[].class );
                m.setAccessible( true );
                m.invoke( null, new Object[] { args } );
            }
        }, sychronize );
    }

    public Result optimize( final IOptimizerConfiguration configuration, boolean sychronize )
        throws Exception
    {
        return CommandUtil.execute( new Command()
        {
            public void command()
                throws Exception
            {
                String[] args = parser.parseArguments( configuration, IOptimizerConfiguration.class );
                logArgs( args );
                try {
                    executeOptimizerMain(args);
                } catch (Throwable t) {
                    throw new Exception("Exception during Optimizer execution", t);
                }
            }
        }, sychronize );
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
            synchronized ( getLogger() )
            {
                getLogger().debug( "Compilation arguments:" + sb );
            }
        }
    }

    private static MethodHandle compcMain;
    private void executeCompcMain(String[] args) throws Throwable {
        if(compcMain == null) {
            Method compcMainReflect;
            try {
                Class<?> compc = Class.forName("org.apache.flex.compiler.clients.COMPC");
                compcMainReflect = compc.getMethod("main", String[].class);
            } catch (Exception e) {
                try {
                    Class<?> compc = Class.forName("flex2.tools.Compc");
                    compcMainReflect = compc.getMethod("main", String[].class);
                } catch (Exception e1) {
                    throw new Exception("Could not find 'org.apache.flex.compiler.clients.COMPC' or " +
                            "'flex2.tools.Compc' in the current projects classpath.");
                }
            }
            compcMain = MethodHandles.lookup().unreflect(compcMainReflect);
        }
        if(compcMain == null) {
            throw new Exception("Could not find static main method on compc implementation class.");
        }
        compcMain.invoke( args );
    }

    private static MethodHandle mxmlcMain;
    private void executeMxmlcMain(String[] args) throws Throwable {
        if(mxmlcMain == null) {
            Method mxmlcMainReflect;
            try {
                Class<?> mxmlc = Class.forName("org.apache.flex.compiler.clients.MXMLC");
                mxmlcMainReflect = mxmlc.getMethod("main", String[].class);
            } catch (Exception e) {
                try {
                    Class<?> mxmlc = Class.forName("flex2.tools.Mxmlc");
                    mxmlcMainReflect = mxmlc.getMethod("main", String[].class);
                } catch (Exception e1) {
                    throw new Exception("Could not find 'org.apache.flex.compiler.clients.MXMLC' or " +
                            "'flex2.tools.Mxmlc' in the current projects classpath.");
                }
            }
            mxmlcMain = MethodHandles.lookup().unreflect(mxmlcMainReflect);
        }
        if(mxmlcMain == null) {
            throw new Exception("Could not find static main method on mxmlc implementation class.");
        }
        mxmlcMain.invoke( args );
    }


    private static MethodHandle optimizerMain;
    private void executeOptimizerMain(String[] args) throws Throwable {
        if(optimizerMain == null) {
            Method optimizerMainReflect;
            try {
                Class<?> optimizer = Class.forName("org.apache.flex.compiler.clients.Optimizer");
                optimizerMainReflect = optimizer.getMethod("main", String[].class);
            } catch (Exception e) {
                try {
                    Class<?> optimizer = Class.forName("flex2.tools.Optimizer");
                    optimizerMainReflect = optimizer.getMethod("main", String[].class);
                } catch (Exception e1) {
                    throw new Exception("Could not find 'org.apache.flex.compiler.clients.Optimizer' or " +
                            "'flex2.tools.Optimizer' in the current projects classpath.");
                }
            }
            optimizerMain = MethodHandles.lookup().unreflect(optimizerMainReflect);
        }
        if(optimizerMain == null) {
            throw new Exception("Could not find static main method on optimizer implementation class.");
        }
        optimizerMain.invoke( args );
    }

}
