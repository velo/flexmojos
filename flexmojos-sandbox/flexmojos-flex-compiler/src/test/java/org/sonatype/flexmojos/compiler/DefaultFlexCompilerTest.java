package org.sonatype.flexmojos.compiler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonatype.flexmojos.compiler.test.MockitoConstraints.RETURNS_NULL;

import java.io.File;
import java.util.Collections;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.sonatype.flexmojos.compiler.util.ThreadLocalToolkitHelper;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import flex2.compiler.common.SinglePathResolver;
import flex2.compiler.util.ConsoleLogger;

public class DefaultFlexCompilerTest
{

    private PlexusContainer plexus;

    private File root;

    private File as3;

    private File fdk;

    @BeforeClass
    public void init()
        throws PlexusContainerException
    {
        plexus = new DefaultPlexusContainer();
    }

    @BeforeMethod
    public void initRoots()
        throws Exception
    {
        root = new File( "" ).getCanonicalFile();
        as3 = new File( root, "dummy_as3" );
        fdk = new File( root, "fdk" );

        ThreadLocalToolkitHelper.setMavenLogger( new ConsoleLogger() );
        ThreadLocalToolkitHelper.setMavenResolver( mock( SinglePathResolver.class ) );
    }

    @Test
    public void compileDummySwc()
        throws Exception
    {
        File output = new File( as3, "result.swc" );

        DefaultFlexCompiler compiler = (DefaultFlexCompiler) plexus.lookup( FlexCompiler.class );

        ICompcConfiguration cfg = mock( ICompcConfiguration.class, RETURNS_NULL );
        ICompilerConfiguration compilerCfg = getBaseCompilerCfg();
        when( cfg.getIncludeSources() ).thenReturn( new File[] { as3 } );
        when( cfg.getLoadConfig() ).thenReturn( new String[] {} );
        when( cfg.getOutput() ).thenReturn( output.getAbsolutePath() );
        when( cfg.getCompilerConfiguration() ).thenReturn( compilerCfg );
        Assert.assertEquals( compiler.compileSwc( cfg, true ).getExitCode(), 0 );
    }

    @Test
    public void compileDummySwf()
        throws Exception
    {
        File output = new File( as3, "result.swf" );

        DefaultFlexCompiler compiler = (DefaultFlexCompiler) plexus.lookup( FlexCompiler.class );
        MapLogger logger = new MapLogger();
        compiler.enableLogging( logger );

        ICommandLineConfiguration cfg = mock( ICommandLineConfiguration.class, RETURNS_NULL );
        ICompilerConfiguration compilerCfg = getBaseCompilerCfg();
        when( cfg.getLoadConfig() ).thenReturn( new String[] {} );
        when( cfg.getOutput() ).thenReturn( output.getAbsolutePath() );
        when( cfg.getCompilerConfiguration() ).thenReturn( compilerCfg );
        Assert.assertEquals(
                             compiler.compileSwf( new MxmlcConfigurationHolder( cfg, new File( as3, "main.as" ) ), true ).getExitCode(),
                             0 );

        Assert.assertTrue( output.exists(), logger.getLogs().toString() );
    }

    private ICompilerConfiguration getBaseCompilerCfg()
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
