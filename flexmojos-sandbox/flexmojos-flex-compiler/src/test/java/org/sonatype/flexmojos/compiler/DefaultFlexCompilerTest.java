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
