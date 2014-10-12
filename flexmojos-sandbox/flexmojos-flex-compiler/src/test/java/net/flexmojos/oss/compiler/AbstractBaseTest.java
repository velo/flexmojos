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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static net.flexmojos.oss.compiler.test.MockitoConstraints.RETURNS_NULL;

import java.io.File;
import java.util.Collections;

import org.codehaus.plexus.*;
import net.flexmojos.oss.compiler.util.ThreadLocalToolkitHelper;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import flex2.compiler.common.SinglePathResolver;
import flex2.compiler.util.ConsoleLogger;

public abstract class AbstractBaseTest
{

    protected PlexusContainer plexus;

    protected File root;

    protected File as3;

    protected File fdk;

    public AbstractBaseTest()
    {
        super();
    }

    @BeforeClass
    public void init()
        throws PlexusContainerException
    {
        ContainerConfiguration config = new DefaultContainerConfiguration();
        config.setAutoWiring(true);
        config.setClassPathScanning(PlexusConstants.SCANNING_ON);
        plexus = new DefaultPlexusContainer(config);
    }

    @BeforeMethod
    public void initRoots()
        throws Exception
    {
        root = new File( "target/test-classes" ).getCanonicalFile();
        as3 = new File( root, "dummy_as3" );
        fdk = new File( root, "fdk" );

        ThreadLocalToolkitHelper.setMavenLogger( new ConsoleLogger() );
        ThreadLocalToolkitHelper.setMavenResolver( mock( SinglePathResolver.class ) );
    }

    protected ICompilerConfiguration getBaseCompilerCfg()
    {
        ICompilerConfiguration compilerCfg = mock( ICompilerConfiguration.class, RETURNS_NULL );
        IFontsConfiguration fontsCfg = mock( IFontsConfiguration.class, RETURNS_NULL );
        when( compilerCfg.getFontsConfiguration() ).thenReturn( fontsCfg );
        when( fontsCfg.getLocalFontsSnapshot() ).thenReturn( new File( fdk, "localFonts.ser" ).getAbsolutePath() );
        when( compilerCfg.getExternalLibraryPath() ).thenReturn( new File[] { new File( fdk, "playerglobal.swc" ) } );
        when( compilerCfg.getTheme() ).thenReturn( Collections.emptyList() );
        return compilerCfg;
    }

}