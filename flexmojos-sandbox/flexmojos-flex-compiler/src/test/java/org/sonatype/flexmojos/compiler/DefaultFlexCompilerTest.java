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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.sonatype.flexmojos.compiler.util.ParseArguments;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DefaultFlexCompilerTest
{

    private PlexusContainer plexus;

    @BeforeClass
    public void init()
        throws PlexusContainerException
    {
        plexus = new DefaultPlexusContainer();
    }

    @Test
    public void logTest()
        throws Exception
    {
        DefaultFlexCompiler compiler = (DefaultFlexCompiler) plexus.lookup( FlexCompiler.class );
        MapLogger logger = new MapLogger();
        compiler.enableLogging( logger );

        compiler.compileSwc( null );

        Assert.assertFalse( logger.getLogs().isEmpty() );
    }

    @Test
    public void simpleCfgParse()
        throws Exception
    {
        ICompcConfiguration cfg = mock( ICompcConfiguration.class );
        when( cfg.getDebugPassword() ).thenReturn( "dbgPw" );

        List<String> args = ParseArguments.getArguments( cfg );
        Assert.assertNotNull( args );
        Assert.assertEquals( args.size(), 1, args.toString() );
        Assert.assertEquals( args.get( 0 ), "debug-password=dbgPw" );
    }

}
