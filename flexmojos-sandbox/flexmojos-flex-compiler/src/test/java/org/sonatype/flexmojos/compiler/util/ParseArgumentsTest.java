package org.sonatype.flexmojos.compiler.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionContaining.hasItem;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonatype.flexmojos.compiler.test.MockitoConstraints.RETURNS_NULL;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.flexmojos.compiler.ICompcConfiguration;
import org.sonatype.flexmojos.compiler.ICompilerConfiguration;
import org.sonatype.flexmojos.compiler.IFontsConfiguration;
import org.sonatype.flexmojos.compiler.IFrame;
import org.sonatype.flexmojos.compiler.IFramesConfiguration;
import org.sonatype.flexmojos.compiler.ILanguageRange;
import org.sonatype.flexmojos.compiler.ILanguages;
import org.sonatype.flexmojos.compiler.IMetadataConfiguration;
import org.sonatype.flexmojos.compiler.IRuntimeSharedLibraryPath;
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

        List<String> args = ParseArguments.getArgumentsList( cfg, ICompcConfiguration.class );

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
        IRuntimeSharedLibraryPath runtimeCfg = mock( IRuntimeSharedLibraryPath.class, RETURNS_NULL );
        Map<String, String> urls = new LinkedHashMap<String, String>();
        urls.put( "http://a.com/rsls/MyLibrary.swf", "http://a.com/rsls/crossdomain.xml" );
        urls.put( "MyLibrary.swf", null );
        IFramesConfiguration frameCfg = mock( IFramesConfiguration.class, RETURNS_NULL );
        IFrame frame = mock( IFrame.class, RETURNS_NULL );
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
        when( cfg.getRuntimeSharedLibraryPath() ).thenReturn( new IRuntimeSharedLibraryPath[] { runtimeCfg } );
        when( runtimeCfg.pathElement() ).thenReturn( "MyLibrary.swc" );
        when( runtimeCfg.rslUrl() ).thenReturn( urls );
        when( cfg.getFramesConfiguration() ).thenReturn( frameCfg );
        when( frameCfg.getFrame() ).thenReturn( new IFrame[] { frame } );
        when( frame.label() ).thenReturn( "my-frame" );
        when( frame.classname() ).thenReturn( new String[] { "org.package.1", "org.package.2" } );

        List<String> args = ParseArguments.getArgumentsList( cfg, ICompcConfiguration.class );

        Assert.assertNotNull( args );
        Assert.assertEquals( args.size(), 8, args.toString() );
        Assert.assertTrue( args.contains( "-compiler.accessible=true" ) );
        Assert.assertTrue( args.contains( "-compiler.fonts.languages.language-range Thai U+0E01-0E5B" ) );
        Assert.assertTrue( args.contains( "-compiler.fonts.languages.language-range ptBR U+0A0C-0EAA" ) );
        Assert.assertTrue( args.contains( "-metadata.creator=Marvin" ) );
        Assert.assertTrue( args.contains( "-metadata.creator+=VELO" ) );
        Assert.assertTrue( args.contains( "-metadata.creator+=Froeder" ) );
        assertThat(
                    args,
                    hasItem( "-runtime-shared-library-path MyLibrary.swc http://a.com/rsls/MyLibrary.swf http://a.com/rsls/crossdomain.xml MyLibrary.swf" ) );
        assertThat( args, hasItem( "-frames.frame my-frame org.package.1 org.package.2" ) );
    }

    @Test
    public void emptyCfgParse()
        throws Exception
    {
        ICompcConfiguration cfg = mock( ICompcConfiguration.class, RETURNS_NULL );
        when( cfg.getLoadConfig() ).thenReturn( new String[] {} );

        List<String> args = ParseArguments.getArgumentsList( cfg, ICompcConfiguration.class );

        Assert.assertNotNull( args );
        Assert.assertEquals( args.size(), 1, args.toString() );
        Assert.assertTrue( args.contains( "-load-config=" ) );
    }
}
