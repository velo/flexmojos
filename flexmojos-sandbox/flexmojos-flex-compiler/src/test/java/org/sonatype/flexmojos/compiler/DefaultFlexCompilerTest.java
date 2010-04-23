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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonatype.flexmojos.compiler.test.MockitoConstraints.RETURNS_NULL;

import java.io.File;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
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
    public void compileDummySwc()
        throws Exception
    {
        File root = new File( getClass().getResource( "/" ).toURI() );
        File as3 = new File( root, "dummy_as3" );
        File fdk = new File( root, "fdk" );
        File output = new File( as3, "result.swc" );

        DefaultFlexCompiler compiler = (DefaultFlexCompiler) plexus.lookup( FlexCompiler.class );
        MapLogger logger = new MapLogger();
        compiler.enableLogging( logger );

        ICompcConfiguration cfg = mock( ICompcConfiguration.class, RETURNS_NULL );
        ICompilerConfiguration compilerCfg = mock( ICompilerConfiguration.class, RETURNS_NULL );
        IFontsConfiguration fontsCfg = mock( IFontsConfiguration.class, RETURNS_NULL );
        when( cfg.getIncludeSources() ).thenReturn( new File[] { as3 } );
        when( cfg.getLoadConfig() ).thenReturn( new String[] {} );
        when( cfg.getOutput() ).thenReturn( output.getAbsolutePath() );
        when( cfg.getCompilerConfiguration() ).thenReturn( compilerCfg );
        when( compilerCfg.getFontsConfiguration() ).thenReturn( fontsCfg );
        when( compilerCfg.getExternalLibraryPath() ).thenReturn( new File[] { new File( fdk, "playerglobal.swc" ) } );
        when( fontsCfg.getLocalFontsSnapshot() ).thenReturn( new File( fdk, "localFonts.ser" ).getAbsolutePath() );
        compiler.compileSwc( cfg );

        Assert.assertTrue( output.exists(), logger.getLogs().toString() );
    }

    @Test
    public void compileDummySwf()
        throws Exception
    {
        File root = new File( getClass().getResource( "/" ).toURI() );
        File as3 = new File( root, "dummy_as3" );
        File fdk = new File( root, "fdk" );
        File output = new File( as3, "result.swf" );

        DefaultFlexCompiler compiler = (DefaultFlexCompiler) plexus.lookup( FlexCompiler.class );
        MapLogger logger = new MapLogger();
        compiler.enableLogging( logger );

        ICommandLineConfiguration cfg = mock( ICommandLineConfiguration.class, RETURNS_NULL );
        ICompilerConfiguration compilerCfg = mock( ICompilerConfiguration.class, RETURNS_NULL );
        IFontsConfiguration fontsCfg = mock( IFontsConfiguration.class, RETURNS_NULL );
        when( cfg.getLoadConfig() ).thenReturn( new String[] {} );
        when( cfg.getOutput() ).thenReturn( output.getAbsolutePath() );
        when( cfg.getCompilerConfiguration() ).thenReturn( compilerCfg );
        when( compilerCfg.getFontsConfiguration() ).thenReturn( fontsCfg );
        when( compilerCfg.getExternalLibraryPath() ).thenReturn( new File[] { new File( fdk, "playerglobal.swc" ) } );
        when( fontsCfg.getLocalFontsSnapshot() ).thenReturn( new File( fdk, "localFonts.ser" ).getAbsolutePath() );
        compiler.compileSwf( cfg, new File( as3, "main.as" ) );

        Assert.assertTrue( output.exists(), logger.getLogs().toString() );
    }
}
