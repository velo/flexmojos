package org.sonatype.flexmojos.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PathUtilTest
{

    private static final String _PATH = "/this/is/a/full//path";

    private static final String ABSOLUTE_PATH = "absolute/path";

    private File f1;

    private File f2;

    private File f3;

    private File f4;

    private File f5;

    @BeforeClass
    public void createFiles()
        throws IOException
    {
        f1 = mock( File.class );
        when( f1.getCanonicalPath() ).thenReturn( _PATH );
        when( f1.exists() ).thenReturn( true );

        f2 = mock( File.class );
        when( f2.getCanonicalPath() ).thenThrow( new IOException() );
        when( f2.getAbsolutePath() ).thenReturn( ABSOLUTE_PATH );
        when( f2.exists() ).thenReturn( false );

        f3 = mock( File.class );
        when( f3.getCanonicalPath() ).thenReturn( _PATH );
        when( f3.getCanonicalFile() ).thenReturn( f1 );
        when( f3.exists() ).thenReturn( true );

        f4 = mock( File.class );
        when( f4.getCanonicalPath() ).thenThrow( new IOException() );
        when( f4.getCanonicalFile() ).thenThrow( new IOException() );
        when( f4.getAbsolutePath() ).thenReturn( ABSOLUTE_PATH );
        when( f4.getAbsoluteFile() ).thenReturn( f2 );
        when( f4.exists() ).thenReturn( false );

        f5 = new File( getClass().getResource( "." ).getFile() ).getCanonicalFile();
    }

    @Test
    public void testExistAllFileArray()
    {
        assertThat( PathUtil.existAll(), equalTo( false ) );
        assertThat( PathUtil.existAll( (File[]) null ), equalTo( false ) );
        assertThat( PathUtil.existAll( f1 ), equalTo( true ) );
        assertThat( PathUtil.existAll( f2 ), equalTo( false ) );
        assertThat( PathUtil.existAll( f1, f3 ), equalTo( true ) );
        assertThat( PathUtil.existAll( f1, f2, f4 ), equalTo( false ) );
    }

    @Test
    public void testExistAllListOfString()
    {
        assertThat( PathUtil.existAll( (List<String>) null ), equalTo( false ) );
        assertThat( PathUtil.existAll( Arrays.asList( f5.getAbsolutePath() ) ), equalTo( true ) );
    }

    @Test
    public void testExistAnyFileArray()
    {
        assertThat( PathUtil.existAny(), equalTo( false ) );
        assertThat( PathUtil.existAny( (File[]) null ), equalTo( false ) );
        assertThat( PathUtil.existAny( f1 ), equalTo( true ) );
        assertThat( PathUtil.existAny( f2 ), equalTo( false ) );
        assertThat( PathUtil.existAny( f2, f4, f1 ), equalTo( true ) );
    }

    @Test
    public void testGetExistingFilesCollectionOfString()
    {
        assertThat( PathUtil.existingFiles( (List<String>) null ), nullValue() );
        assertThat( PathUtil.existingFiles( Collections.singletonList( f5.getAbsolutePath() ) ),
                    equalTo( new File[] { f5 } ) );
    }

    @Test
    public void testGetExistingFilesFileArray()
    {
        assertThat( PathUtil.existingFiles( (File[]) null ), nullValue() );
        assertThat( PathUtil.existingFiles( f5 ), equalTo( new File[] { f5 } ) );
    }

    @Test
    public void testGetExistingFilesListCollectionOfString()
    {
        assertThat( PathUtil.existingFilesList( (List<String>) null ), nullValue() );
        assertThat( PathUtil.existingFilesList( Collections.singletonList( f5.getAbsolutePath() ) ), hasItem( f5 ) );
    }

    @Test
    public void testGetExistingFilesListListOfFile()
    {
        assertThat( PathUtil.existingFilesList( (List<File>) null ), nullValue() );
        assertThat( PathUtil.existingFilesList( Collections.singletonList( f5 ) ), hasItem( f5 ) );
        assertThat( PathUtil.existingFilesList( Arrays.asList( f2, f4, f5 ) ), hasItem( f5 ) );
    }

    @Test
    public void testGetFile()
        throws IOException
    {
        assertThat( PathUtil.file( (String) null ), nullValue() );
        assertThat( PathUtil.file( _PATH ), equalTo( new File( _PATH ).getCanonicalFile() ) );
    }

    @Test
    public void testGetFileFile()
    {
        assertThat( PathUtil.file( (File) null ), nullValue() );
        assertThat( PathUtil.file( f3 ), equalTo( f1 ) );
        assertThat( PathUtil.file( f4 ), equalTo( f2 ) );
    }

    @Test
    public void testGetFilesList()
    {
        assertThat( PathUtil.files( (Collection<String>) null ), nullValue() );
        assertThat( PathUtil.files( Arrays.asList( f5.getAbsolutePath() ) ), equalTo( new File[] { f5 } ) );
    }

    @Test
    public void testGetFilesListCollectionOfString()
    {
        assertThat( PathUtil.filesList( (Collection<String>) null ), nullValue() );
        assertThat( PathUtil.filesList( Arrays.asList( f5.getAbsolutePath() ) ), hasItem( f5 ) );
    }

    @Test
    public void testGetFilesListOfStringFile()
    {
        assertThat( PathUtil.files( (List<String>) null, f1 ), nullValue() );
        assertThat( PathUtil.files( Collections.singletonList( f5.getAbsolutePath() ), f1 ), hasItem( f5 ) );
        assertThat( PathUtil.files( Collections.singletonList( f5.getName() ), f5.getParentFile() ), hasItem( f5 ) );
    }

    @Test
    public void testGetFilesStringArray()
    {
        assertThat( PathUtil.files( (String[]) null ), nullValue() );
        assertThat( PathUtil.files( new String[] { f5.getAbsolutePath() } ), equalTo( new File[] { f5 } ) );
    }

    @Test
    public void testGetFilesStringArrayFile()
    {
        assertThat( PathUtil.files( (String[]) null, f1 ), nullValue() );
        assertThat( PathUtil.files( new String[] { f5.getAbsolutePath() }, f1 ), hasItem( f5 ) );
        assertThat( PathUtil.files( new String[] { f5.getName() }, f5.getParentFile() ), hasItem( f5 ) );
    }

    @Test
    public void testGetFileStringFile()
    {
        assertThat( PathUtil.file( null, f1 ), nullValue() );
        assertThat( PathUtil.file( f5.getAbsolutePath(), f1 ), equalTo( f5 ) );
        assertThat( PathUtil.file( f5.getName(), f5.getParentFile() ), equalTo( f5 ) );
    }

    @Test
    public void testGetFileStringFileArray()
    {
        assertThat( PathUtil.file( null, f5, f1 ), nullValue() );
        assertThat( PathUtil.file( f5.getName(), f5.getParentFile(), f1 ), equalTo( f5 ) );
        assertThat( PathUtil.file( f5.getAbsolutePath(), f1, f1 ), equalTo( f5 ) );
    }

    @Test
    public void testGetFileStringListOfFile()
    {
        assertThat( PathUtil.file( null, Collections.singletonList( f5 ) ), nullValue() );
        assertThat( PathUtil.file( f5.getName(), Collections.singletonList( f5.getParentFile() ) ), equalTo( f5 ) );
        assertThat( PathUtil.file( f5.getAbsolutePath(), Collections.singletonList( f1 ) ), equalTo( f5 ) );
        assertThat( PathUtil.file( f5.getName(), new ArrayList<File>() ), nullValue() );
    }

    @Test
    public void testGetPath()
        throws IOException
    {
        assertThat( PathUtil.path( null ), nullValue() );
        assertThat( PathUtil.path( f1 ), equalTo( _PATH ) );
        assertThat( PathUtil.path( f2 ), equalTo( ABSOLUTE_PATH ) );
    }

    @Test
    public void testGetPathsCollectionOfFile()
    {
        assertThat( PathUtil.paths( (Collection<File>) null ), nullValue() );
        assertThat( PathUtil.paths( Arrays.asList( f1, f2 ) ), equalTo( new String[] { _PATH, ABSOLUTE_PATH } ) );
    }

    @Test
    public void testGetPathsFileArray()
    {
        assertThat( PathUtil.paths( (File[]) null ), nullValue() );
        assertThat( PathUtil.paths().length, equalTo( 0 ) );
    }

    @Test
    public void testGetPathsList()
    {
        assertThat( PathUtil.pathsList( (File[]) null ), nullValue() );
        assertThat( PathUtil.pathsList( new File[] { f1, f2 } ), hasItems( _PATH, ABSOLUTE_PATH ) );
    }

    @Test
    public void testPathsList()
    {
        assertThat( PathUtil.pathsList( (List<File>) null ), nullValue() );
        assertThat( PathUtil.pathsList( Arrays.asList( f1, f2 ) ), hasItems( _PATH, ABSOLUTE_PATH ) );
    }

    @Test
    public void testGetPathString()
    {
        assertThat( PathUtil.pathString( null ), nullValue() );
        assertThat( PathUtil.pathString( new File[] { f1 } ), equalTo( _PATH ) );
        assertThat( PathUtil.pathString( new File[] { f1, f2 } ), equalTo( _PATH + File.pathSeparatorChar
            + ABSOLUTE_PATH ) );
    }

    @Test
    public void testGetRelativePath()
    {
        assertThat( PathUtil.relativePath( new File( "/home" ), new File( "/home/velo/content" ) ),
                    equalTo( "velo/content" ) );
        assertThat( PathUtil.relativePath( new File( "/home/velo/content" ), new File( "/home/velo/c2" ) ),
                    equalTo( "../c2" ) );
        assertThat( PathUtil.relativePath( new File( "/home/velo/content" ), new File( "/home/velo2" ) ),
                    equalTo( "../../velo2" ) );
    }

}
