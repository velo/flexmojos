/**
 * Flexmojos is a set of maven goals to allow maven users to compile,
 * optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import net.flexmojos.oss.compiler.interceptor.FlexToolInterceptor;
import org.apache.flex.tools.FlexTool;
import org.apache.flex.tools.FlexToolGroup;
import org.apache.flex.tools.FlexToolRegistry;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import net.flexmojos.oss.compiler.command.Command;
import net.flexmojos.oss.compiler.command.CommandUtil;
import net.flexmojos.oss.compiler.command.Result;
import net.flexmojos.oss.compiler.util.FlexCompilerArgumentParser;

@Component( role = FlexCompiler.class )
public class DefaultFlexCompiler
    extends AbstractLogEnabled
    implements FlexCompiler
{

    @Requirement
    private FlexCompilerArgumentParser parser;

    @Requirement( role = FlexToolInterceptor.class )
    private List<FlexToolInterceptor> interceptors;

    public Result compileSwc( final ICompcConfiguration configuration, boolean sychronize,
                              final String compilerName )
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
                    executeCompcMain(compilerName, args);
                } catch (Throwable t) {
                    throw new Exception("Exception during Compc execution", t);
                }
            }
        }, sychronize );
    }

    public Result compileSwf( MxmlcConfigurationHolder cfgHolder, boolean sychronize,
                              final String compilerName )
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
                    executeMxmlcMain(compilerName, args);
                } catch (Throwable t) {
                    throw new Exception("Exception during Mxmlc execution", t);
                }
            }
        }, sychronize );
    }

    public Result asdoc( final IASDocConfiguration configuration, boolean sychronize,
                         final String compilerName )
        throws Exception
    {
        return CommandUtil.execute( new Command()
        {
            public void command()
                    throws Exception
            {
                String[] args = parser.parseArguments(configuration, IASDocConfiguration.class);
                logArgs(args);
                try {
                    executeAsdocMain(compilerName, args);
                } catch(Throwable t) {
                    throw new Exception("Exception during ASDoc execution", t);
                }
            }
        }, sychronize );
    }

    public Result digest( final IDigestConfiguration configuration, boolean sychronize,
                          final String compilerName )
        throws Exception
    {
        return CommandUtil.execute( new Command()
        {
            public void command()
                throws Exception
            {
                String[] args = parser.parseArguments( configuration, IDigestConfiguration.class );
                logArgs( args );
                try {
                    executeDigestMain(compilerName, args);
                } catch (Throwable t) {
                    throw new Exception("Exception during DigestTool execution", t);
                }
            }
        }, sychronize );
    }

    public Result optimize( final IOptimizerConfiguration configuration, boolean sychronize,
                            final String compilerName )
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
                    executeOptimizerMain(compilerName, args);
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

    private void executeCompcMain(String compilerName, String[] args) throws Throwable {
        if(compilerName != null) {
            executeTool(compilerName, FlexTool.FLEX_TOOL_COMPC, args);
        } else {
            try {
                Class<?> compc = Class.forName("flex2.tools.Compc");
                Method compcMain = compc.getMethod("compc", String[].class);
                compcMain.invoke( null, new Object[] {args} );
            } catch (Exception e1) {
                throw new Exception("Could not find 'org.apache.flex.compiler.clients.COMPC' or " +
                        "'flex2.tools.Compc' in the current projects classpath.");
            }
        }
    }

    private void executeMxmlcMain(String compilerName, String[] args) throws Throwable {
        if(compilerName != null) {
            executeTool(compilerName, FlexTool.FLEX_TOOL_MXMLC, args);
        } else {
            try {
                Class<?> mxmlc = Class.forName("flex2.tools.Mxmlc");
                Method mxmlcMain = mxmlc.getMethod("mxmlc", String[].class);
                mxmlcMain.invoke(null, new Object[]{args});
            } catch (Exception e1) {
                throw new Exception("Could not find 'org.apache.flex.compiler.clients.MXMLC' or " +
                        "'flex2.tools.Mxmlc' in the current projects classpath.");
            }
        }
    }


    private void executeAsdocMain(String compilerName, String[] args) throws Throwable {
        if(compilerName != null) {
            executeTool(compilerName, FlexTool.FLEX_TOOL_ASDOC, args);
        } else {
            try {
                Class<?> asdoc = Class.forName("flex2.tools.ASDoc");
                Method asdocMain = asdoc.getMethod("asdoc", String[].class);

                String defaultTransformer = null;
                try {
                    // Force the XML Transformer to the Xalan version that comes with Flex
                    defaultTransformer = System.getProperty("javax.xml.transform.TransformerFactory");
                    System.setProperty("javax.xml.transform.TransformerFactory",
                            "org.apache.xalan.processor.TransformerFactoryImpl");

                    asdocMain.invoke( null, new Object[] {args} );
                } finally {
                    // and set it back to the default
                    if (defaultTransformer == null) {
                        System.getProperties().remove("javax.xml.transform.TransformerFactory");
                    } else {
                        System.setProperty("javax.xml.transform.TransformerFactory", defaultTransformer);
                    }
                }
            } catch (Exception e1) {
                throw new Exception("Could not find 'flex2.tools.ASDoc' " +
                        "in the current projects classpath.");
            }
        }
    }


    private void executeDigestMain(String compilerName, String[] args) throws Throwable {
        if(compilerName != null) {
            executeTool(compilerName, FlexTool.FLEX_TOOL_DIGEST, args);
        } else {
            try {
                Class<?> digest = Class.forName("flex2.tools.DigestTool");
                Method digestMain = digest.getDeclaredMethod("digestTool", String[].class);
                digestMain.setAccessible(true);
                digestMain.invoke( null, new Object[] {args} );
            } catch (Exception e1) {
                throw new Exception("Could not find 'flex2.tools.DigestTool' " +
                        "in the current projects classpath.", e1);
            }
        }
    }

    private void executeOptimizerMain(String compilerName, String[] args) throws Throwable {
        if(compilerName != null) {
            executeTool(compilerName, FlexTool.FLEX_TOOL_OPTIMIZER, args);
        } else {
            try {
                Class<?> optimizer = Class.forName("flex2.tools.Optimizer");
                Method optimizerMain = optimizer.getMethod("main", String[].class);
                optimizerMain.invoke(null, new Object[]{args});
            } catch (Exception e1) {
                throw new Exception("Could not find 'org.apache.flex.compiler.clients.Optimizer' or " +
                        "'flex2.tools.Optimizer' in the current projects classpath.");
            }
        }
    }

    private int executeTool(String toolGroupName, String toolName, String[] args) throws Exception {
        // Initialize the tool registry.
        FlexToolRegistry toolRegistry = new FlexToolRegistry();

        // Get the desired tool group.
        FlexToolGroup toolGroup = toolRegistry.getToolGroup(toolGroupName);
        if(toolGroup == null) {
            throw new Exception("Unable to find compiler: " + toolGroupName + " in the plugin classpath. " +
                    "List of compiles found: " + toolRegistry.getToolGroupNames());
        }

        // Get the desired tool.
        if(!toolGroup.hasFlexTool(toolName)) {
            throw new Exception("Compiler: " + toolGroupName + " doesn't provide a tool named " + toolName);
        }
        FlexTool tool = toolGroup.getFlexTool(toolName);

        // If interceptors are provided, let each one process the argument list.
        if(interceptors != null) {
            for(FlexToolInterceptor interceptor : interceptors) {
                args = interceptor.intercept(toolGroup, tool, args);
            }
        }

        // Finally execute the tool.
        return tool.execute(args);
    }

}
