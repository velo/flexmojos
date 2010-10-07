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
        plexus = new DefaultPlexusContainer();
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