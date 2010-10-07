package org.sonatype.flexmojos.compiler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonatype.flexmojos.compiler.test.MockitoConstraints.RETURNS_NULL;

import java.io.File;
import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

public class Flexmojos352Test
    extends AbstractBaseTest
{

    @Test
    public void checkFontManagers()
        throws Exception
    {
        File output = new File( as3, "result.swc" );

        DefaultFlexCompiler compiler = (DefaultFlexCompiler) plexus.lookup( FlexCompiler.class );

        ICompcConfiguration cfg = mock( ICompcConfiguration.class, RETURNS_NULL );
        ICompilerConfiguration compilerCfg = getBaseCompilerCfg();
        IFontsConfiguration fontCfg = compilerCfg.getFontsConfiguration();
        when( cfg.getIncludeSources() ).thenReturn( new File[] { as3 } );
        when( cfg.getLoadConfig() ).thenReturn( new String[] {} );
        when( cfg.getOutput() ).thenReturn( output.getAbsolutePath() );
        when( cfg.getCompilerConfiguration() ).thenReturn( compilerCfg );
        when( compilerCfg.getFontsConfiguration() ).thenReturn( fontCfg );
        when( fontCfg.getManagers() ).thenReturn( Arrays.asList( "flash.fonts.JREFontManager",
                                                                 "flash.fonts.BatikFontManager",
                                                                 "flash.fonts.AFEFontManager",
                                                                 "flash.fonts.CFFFontManager" ) );
        Assert.assertEquals( compiler.compileSwc( cfg, true ).getExitCode(), 0 );
    }

}
