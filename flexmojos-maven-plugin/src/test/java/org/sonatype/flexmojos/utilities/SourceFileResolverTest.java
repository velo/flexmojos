package org.sonatype.flexmojos.utilities;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SourceFileResolverTest
{

    private File root;

    @BeforeClass
    public void createRoot()
        throws URISyntaxException
    {
        root = new File( new File( getClass().getResource( "/" ).toURI() ), "fileresolver" );
    }

    private List<File> getDir( String... names )
    {
        List<File> files = new ArrayList<File>();
        for ( String name : names )
        {
            File dir = new File( root, name );
            Assert.assertTrue( dir.isDirectory(), "Folder " + dir.getAbsolutePath() + " does not exits" );
            files.add( dir );
        }

        return files;
    }

    @Test
    public void resolveSimple()
    {
        File file = SourceFileResolver.resolveSourceFile( "thesimple.as", getDir( "simple" ), null, null );
        Assert.assertTrue( file.exists() );
        MatcherAssert.assertThat( file.getName(), CoreMatchers.equalTo( "thesimple.as" ) );
    }

    @Test
    public void resolveIndexAs()
    {
        File file = SourceFileResolver.resolveSourceFile( null, getDir( "indexas" ), null, null );
        Assert.assertTrue( file.exists() );
        MatcherAssert.assertThat( file.getName(), CoreMatchers.equalTo( "Index.as" ) );
    }

    @Test
    public void resolveIndexMxml()
    {
        File file = SourceFileResolver.resolveSourceFile( null, getDir( "indexmxml" ), null, null );
        Assert.assertTrue( file.exists() );
        MatcherAssert.assertThat( file.getName(), CoreMatchers.equalTo( "Index.mxml" ) );
    }

    @Test
    public void resolveMainAs()
    {
        File file = SourceFileResolver.resolveSourceFile( null, getDir( "mainas" ), null, null );
        Assert.assertTrue( file.exists() );
        MatcherAssert.assertThat( file.getName(), CoreMatchers.equalTo( "Main.as" ) );
    }

    @Test
    public void resolveMainMxml()
    {
        File file = SourceFileResolver.resolveSourceFile( null, getDir( "mainmxml" ), null, null );
        Assert.assertTrue( file.exists() );
        MatcherAssert.assertThat( file.getName(), CoreMatchers.equalTo( "Main.mxml" ) );
    }

    @Test
    public void resolveUniqueAs()
    {
        File file = SourceFileResolver.resolveSourceFile( null, getDir( "uniqueas" ), null, null );
        Assert.assertTrue( file.exists() );
        MatcherAssert.assertThat( file.getName(), CoreMatchers.equalTo( "dummy.as" ) );
    }

    @Test
    public void resolveUniqueMxml()
    {
        File file = SourceFileResolver.resolveSourceFile( null, getDir( "uniquemxml" ), null, null );
        Assert.assertTrue( file.exists() );
        MatcherAssert.assertThat( file.getName(), CoreMatchers.equalTo( "dummy.mxml" ) );
    }

    @Test
    public void resolveArtifactIdAs()
    {
        File file = SourceFileResolver.resolveSourceFile( null, getDir( "artifactidas" ), null, "artifact" );
        Assert.assertTrue( file.exists() );
        MatcherAssert.assertThat( file.getName(), CoreMatchers.equalTo( "artifact.as" ) );
    }

    @Test
    public void resolveArtifactIdMxml()
    {
        File file = SourceFileResolver.resolveSourceFile( null, getDir( "artifactidmxml" ), null, "artifact" );
        Assert.assertTrue( file.exists() );
        MatcherAssert.assertThat( file.getName(), CoreMatchers.equalTo( "artifact.mxml" ) );
    }

    @Test
    public void resolvePackagedGroup()
    {
        File file =
            SourceFileResolver.resolveSourceFile( null, getDir( "packagegroup" ), "org.sonatype.flexmojos", null );
        Assert.assertTrue( file.exists() );
        MatcherAssert.assertThat( file.getName(), CoreMatchers.equalTo( "pack.as" ) );
    }

    @Test
    public void resolvePackagedGroupPlusArtifact()
    {
        File file =
            SourceFileResolver.resolveSourceFile( null, getDir( "packageartifact" ), "org.sonatype.flexmojos",
                                                  "artifactid" );
        Assert.assertTrue( file.exists() );
        MatcherAssert.assertThat( file.getName(), CoreMatchers.equalTo( "pack.mxml" ) );
    }

    @Test
    public void resolveMultipleRoots()
    {
        File file =
            SourceFileResolver.resolveSourceFile( null, getDir( "nroots/root1", "nroots/root2", "nroots/root3" ), null,
                                                  null );
        Assert.assertTrue( file.exists() );
        MatcherAssert.assertThat( file.getName(), CoreMatchers.equalTo( "root.as" ) );
    }

    @Test
    public void resolveAbsolute()
    {
        File file = new File( root, "absolute/xummy.as" );
        Assert.assertTrue( file.exists() );
        File rfile = SourceFileResolver.resolveSourceFile( file.getAbsolutePath(), null, null, null );
        Assert.assertTrue( rfile.exists() );
        MatcherAssert.assertThat( rfile.getName(), CoreMatchers.equalTo( "xummy.as" ) );
        MatcherAssert.assertThat( rfile, CoreMatchers.equalTo( file ) );
    }

    @Test
    public void notResolve()
    {
        File file = SourceFileResolver.resolveSourceFile( "NotResolve.mxml", getDir( "notresolve" ), null, null );
        MatcherAssert.assertThat( file, CoreMatchers.nullValue() );
    }

    @Test
    public void notResolvePackage()
    {
        File file =
            SourceFileResolver.resolveSourceFile( null, getDir( "notresolvepackage" ), "org.sonatype.flexmojos",
                                                  "artifactid" );
        MatcherAssert.assertThat( file, CoreMatchers.nullValue() );
    }

    @Test
    public void notResolvePackageArtifactid()
    {
        File file =
            SourceFileResolver.resolveSourceFile( null, getDir( "notresolvepackageid" ),
                                                  "org.sonatype.flexmojos.artifactid", "artifactid" );
        MatcherAssert.assertThat( file, CoreMatchers.nullValue() );
    }

    @Test
    public void notResolveMultipleRoots()
    {
        File file =
            SourceFileResolver.resolveSourceFile( null, getDir( "notresolvenroots/root1", "notresolvenroots/root2",
                                                                "notresolvenroots/root3" ), null, null );
        MatcherAssert.assertThat( file, CoreMatchers.nullValue() );
    }

    @Test
    public void notResolveNamedMultipleRoots()
    {
        File file =
            SourceFileResolver.resolveSourceFile( "NotResolve.mxml", getDir( "notresolvenroots2/root1",
                                                                             "notresolvenroots2/root2",
                                                                             "notresolvenroots2/root3" ), null, null );
        MatcherAssert.assertThat( file, CoreMatchers.nullValue() );
    }
}
