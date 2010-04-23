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
package org.sonatype.flexmojos.compiler.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonatype.flexmojos.compiler.test.MockitoConstraints.RETURNS_NULL;

import java.util.List;

import org.sonatype.flexmojos.compiler.ICompcConfiguration;
import org.sonatype.flexmojos.compiler.ICompilerConfiguration;
import org.sonatype.flexmojos.compiler.IFontsConfiguration;
import org.sonatype.flexmojos.compiler.ILanguageRange;
import org.sonatype.flexmojos.compiler.ILanguages;
import org.sonatype.flexmojos.compiler.IMetadataConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ParseArgumentsTest
{

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
        ILanguageRange thaiLangRangeCfg = mock( ILanguageRange.class, RETURNS_NULL );
        ILanguageRange ptLangRangeCfg = mock( ILanguageRange.class, RETURNS_NULL );
        IMetadataConfiguration metadataCfg = mock( IMetadataConfiguration.class, RETURNS_NULL );
        when( cfg.getCompilerConfiguration() ).thenReturn( compilerCfg );
        when( cfg.getMetadataConfiguration() ).thenReturn( metadataCfg );
        when( compilerCfg.getAccessible() ).thenReturn( true );
        when( compilerCfg.getFontsConfiguration() ).thenReturn( fontCfg );
        when( fontCfg.getLanguagesConfiguration() ).thenReturn( langsCfg );
        when( langsCfg.getLanguageRange() ).thenReturn( new ILanguageRange[] { thaiLangRangeCfg, ptLangRangeCfg } );
        when( thaiLangRangeCfg.lang() ).thenReturn( "Thai" );
        when( thaiLangRangeCfg.range() ).thenReturn( "U+0E01-0E5B" );
        when( ptLangRangeCfg.lang() ).thenReturn( "ptBR" );
        when( ptLangRangeCfg.range() ).thenReturn( "U+0A0C-0EAA" );
        when( metadataCfg.getCreator() ).thenReturn( new String[] { "Marvin", "VELO", "Froeder" } );

        List<String> args = ParseArguments.getArguments( cfg, ICompcConfiguration.class );

        Assert.assertNotNull( args );
        Assert.assertEquals( args.size(), 6, args.toString() );
        Assert.assertTrue( args.contains( "-compiler.accessible=true" ) );
        Assert.assertTrue( args.contains( "-compiler.fonts.languages.language-range Thai U+0E01-0E5B" ) );
        Assert.assertTrue( args.contains( "-compiler.fonts.languages.language-range ptBR U+0A0C-0EAA" ) );
        Assert.assertTrue( args.contains( "-metadata.creator=Marvin" ) );
        Assert.assertTrue( args.contains( "-metadata.creator+=VELO" ) );
        Assert.assertTrue( args.contains( "-metadata.creator+=Froeder" ) );
    }

    @Test
    public void emptyCfgParse()
        throws Exception
    {
        ICompcConfiguration cfg = mock( ICompcConfiguration.class, RETURNS_NULL );
        when( cfg.getLoadConfig() ).thenReturn( new String[] {} );

        List<String> args = ParseArguments.getArguments( cfg, ICompcConfiguration.class );

        Assert.assertNotNull( args );
        Assert.assertEquals( args.size(), 1, args.toString() );
        Assert.assertTrue( args.contains( "-load-config=" ) );
    }
}
