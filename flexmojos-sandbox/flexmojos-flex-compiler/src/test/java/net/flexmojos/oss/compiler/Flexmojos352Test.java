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
