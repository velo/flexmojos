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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
                    throws Exception
            {
                String[] args = parser.parseArguments(configuration, IASDocConfiguration.class);
                logArgs(args);
                try {
                    executeAsdocMain(args);
                } catch(Throwable t) {
                    throw new Exception("Exception during ASDoc execution", t);
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
                try {
                    executeDigestMain(args);
                } catch (Throwable t) {
                    throw new Exception("Exception during DigestTool execution", t);
                }
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

    private static String compcName;
    private static Method compcMain;
    private void executeCompcMain(String[] args) throws Throwable {
        if(compcMain == null) {
            try {
                Class<?> compc = Class.forName("org.apache.flex.compiler.clients.COMPC");
                compcMain = compc.getMethod("staticMainNoExit", String[].class);
                compcName = "falcon";
            } catch (Exception e) {
                try {
                    Class<?> compc = Class.forName("flex2.tools.Compc");
                    compcMain = compc.getMethod("compc", String[].class);
                    compcName = "default";
                } catch (Exception e1) {
                    throw new Exception("Could not find 'org.apache.flex.compiler.clients.COMPC' or " +
                            "'flex2.tools.Compc' in the current projects classpath.");
                }
            }
        }
        if(compcMain == null) {
            throw new Exception("Could not find static main method on compc implementation class.");
        }

        // Falcon doesn't seem to like empty arguments so we have to remove them first.
        if("falcon".equals(compcName)) {
            List<String> filteredArgs = new ArrayList<String>();
            for(String arg : args) {
                if(!arg.endsWith("=")) {
                    filteredArgs.add(arg);
                }
            }
            args = filteredArgs.toArray(new String[filteredArgs.size()]);
        }

        compcMain.invoke( null, new Object[] {args} );
    }

    private static String mxmlcName;
    private static Method mxmlcMain;
    private void executeMxmlcMain(String[] args) throws Throwable {
        if(mxmlcMain == null) {
            try {
                Class<?> mxmlc = Class.forName("org.apache.flex.compiler.clients.MXMLC");
                mxmlcMain = mxmlc.getMethod("staticMainNoExit", String[].class);
                mxmlcName = "falcon";
            } catch (Exception e) {
                try {
                    Class<?> mxmlc = Class.forName("flex2.tools.Mxmlc");
                    mxmlcMain = mxmlc.getMethod("mxmlc", String[].class);
                    mxmlcName = "default";
                } catch (Exception e1) {
                    throw new Exception("Could not find 'org.apache.flex.compiler.clients.MXMLC' or " +
                            "'flex2.tools.Mxmlc' in the current projects classpath.");
                }
            }
        }
        if(mxmlcMain == null) {
            throw new Exception("Could not find static main method on mxmlc implementation class.");
        }

        // Falcon doesn't seem to like empty arguments so we have to remove them first.
        if("falcon".equals(mxmlcName)) {
            List<String> filteredArgs = new ArrayList<String>();
            for(String arg : args) {
                if(!arg.endsWith("=")) {
                    filteredArgs.add(arg);
                }
            }
            args = filteredArgs.toArray(new String[filteredArgs.size()]);
        }

        mxmlcMain.invoke( null, new Object[] {args} );
    }


    private static Method asdocMain;
    private void executeAsdocMain(String[] args) throws Throwable {
        if(asdocMain == null) {
            try {
                Class<?> asdoc = Class.forName("flex2.tools.ASDoc");
                asdocMain = asdoc.getMethod("asdoc", String[].class);
            } catch (Exception e1) {
                throw new Exception("Could not find 'flex2.tools.ASDoc' " +
                        "in the current projects classpath.");
            }
        }
        if(asdocMain == null) {
            throw new Exception("Could not find static main method on ASDoc implementation class.");
        }

        // Falcon doesn't seem to like empty arguments so we have to remove them first.
        if("falcon".equals(mxmlcName) || "falcon".equals(compcName)) {
            List<String> filteredArgs = new ArrayList<String>();
            for(String arg : args) {
                if(!arg.endsWith("=")) {
                    filteredArgs.add(arg);
                }
            }
            args = filteredArgs.toArray(new String[filteredArgs.size()]);
        }

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
    }


    private static Method digestMain;
    private void executeDigestMain(String[] args) throws Throwable {
        if(digestMain == null) {
            try {
                Class<?> digest = Class.forName("flex2.tools.DigestTool");
                digestMain = digest.getDeclaredMethod("digestTool", String[].class);
                digestMain.setAccessible(true);
            } catch (Exception e1) {
                throw new Exception("Could not find 'flex2.tools.DigestTool' " +
                        "in the current projects classpath.", e1);
            }
        }
        if(digestMain == null) {
            throw new Exception("Could not find static main method on DigestTool implementation class.");
        }

        // Falcon doesn't seem to like empty arguments so we have to remove them first.
        if("falcon".equals(mxmlcName) || "falcon".equals(compcName)) {
            List<String> filteredArgs = new ArrayList<String>();
            for(String arg : args) {
                if(!arg.endsWith("=")) {
                    filteredArgs.add(arg);
                }
            }
            args = filteredArgs.toArray(new String[filteredArgs.size()]);
        }

        digestMain.invoke( null, new Object[] {args} );
    }


    private static Method optimizerMain;
    private void executeOptimizerMain(String[] args) throws Throwable {
        if(optimizerMain == null) {
            try {
                Class<?> optimizer = Class.forName("org.apache.flex.compiler.clients.Optimizer");
                optimizerMain = optimizer.getMethod("staticMainNoExit", String[].class);
            } catch (Exception e) {
                try {
                    Class<?> optimizer = Class.forName("flex2.tools.Optimizer");
                    optimizerMain = optimizer.getMethod("main", String[].class);
                } catch (Exception e1) {
                    throw new Exception("Could not find 'org.apache.flex.compiler.clients.Optimizer' or " +
                            "'flex2.tools.Optimizer' in the current projects classpath.");
                }
            }
        }
        if(optimizerMain == null) {
            throw new Exception("Could not find static main method on optimizer implementation class.");
        }

        // Falcon doesn't seem to like empty arguments so we have to remove them first.
        if("falcon".equals(mxmlcName) || "falcon".equals(compcName)) {
            List<String> filteredArgs = new ArrayList<String>();
            for(String arg : args) {
                if(!arg.endsWith("=")) {
                    filteredArgs.add(arg);
                }
            }
            args = filteredArgs.toArray(new String[filteredArgs.size()]);
        }

        optimizerMain.invoke( null, new Object[] {args} );
    }

}
