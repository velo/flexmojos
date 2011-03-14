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

import java.lang.reflect.Method;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.flexmojos.compiler.command.Command;
import org.sonatype.flexmojos.compiler.command.CommandUtil;
import org.sonatype.flexmojos.compiler.command.Result;
import org.sonatype.flexmojos.compiler.util.FlexCompilerArgumentParser;

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
                Compc.compc( args );
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
            {
                String[] args = argsList.toArray( new String[argsList.size()] );
                logArgs( args );
                Mxmlc.mxmlc( args );
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
                ASDoc.asdoc( args );
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
                Optimizer.main( args );
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

}
