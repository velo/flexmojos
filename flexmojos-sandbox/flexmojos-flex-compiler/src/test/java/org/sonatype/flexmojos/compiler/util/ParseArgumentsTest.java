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
package org.sonatype.flexmojos.compiler.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.mockito.ReturnValues;
import org.mockito.invocation.InvocationOnMock;
import org.sonatype.flexmojos.compiler.ICompcConfiguration;
import org.sonatype.flexmojos.compiler.ICompilerConfiguration;
import org.sonatype.flexmojos.compiler.IFontsConfiguration;
import org.sonatype.flexmojos.compiler.ILanguageRange;
import org.sonatype.flexmojos.compiler.ILanguages;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ParseArgumentsTest
{

    private static final ReturnValues RETURNS_NULL = new ReturnValues()
    {
        public Object valueFor( InvocationOnMock invocation )
        {
            return null;
        }
    };

    @Test
    public void simpleCfgParse()
        throws Exception
    {
        ICompcConfiguration cfg = mock( ICompcConfiguration.class, RETURNS_NULL );
        when( cfg.getDebugPassword() ).thenReturn( "dbgPw" );

        List<String> args = ParseArguments.getArguments( cfg, ICompcConfiguration.class );

        Assert.assertNotNull( args );
        Assert.assertEquals( args.size(), 1, args.toString() );
        Assert.assertEquals( args.get( 0 ), "-debug-password=dbgPw" );
    }

    @Test
    public void compilerCfgParse()
        throws Exception
    {
        ICompcConfiguration cfg = mock( ICompcConfiguration.class, RETURNS_NULL );
        ICompilerConfiguration compilerCfg = mock( ICompilerConfiguration.class, RETURNS_NULL );
        IFontsConfiguration fontCfg = mock( IFontsConfiguration.class, RETURNS_NULL );
        ILanguages langsCfg = mock( ILanguages.class, RETURNS_NULL );
        ILanguageRange langRangeCfg = mock( ILanguageRange.class, RETURNS_NULL );
        when( cfg.getCompilerConfiguration() ).thenReturn( compilerCfg );
        when( compilerCfg.getAccessible() ).thenReturn( true );
        when( compilerCfg.getFontsConfiguration() ).thenReturn( fontCfg );
        when( fontCfg.getLanguagesConfiguration() ).thenReturn( langsCfg );
        when( langsCfg.getLanguageRange() ).thenReturn( new ILanguageRange[] { langRangeCfg } );
        when( langRangeCfg.lang() ).thenReturn( "Thai" );
        when( langRangeCfg.range() ).thenReturn( "U+0E01-0E5B" );

        List<String> args = ParseArguments.getArguments( cfg, ICompcConfiguration.class );

        Assert.assertNotNull( args );
        Assert.assertEquals( args.size(), 2, args.toString() );
        Assert.assertEquals( args.get( 0 ), "-compiler.accessible=true" );
        Assert.assertEquals( args.get( 1 ), "-compiler.fonts.languages.language-range Thai U+0E01-0E5B" );
    }
}
