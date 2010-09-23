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
        assertThat( PathUtil.getExistingFiles( (List<String>) null ), nullValue() );
        assertThat( PathUtil.getExistingFiles( Collections.singletonList( f5.getAbsolutePath() ) ),
                    equalTo( new File[] { f5 } ) );
    }

    @Test
    public void testGetExistingFilesFileArray()
    {
        assertThat( PathUtil.getExistingFiles( (File[]) null ), nullValue() );
        assertThat( PathUtil.getExistingFiles( f5 ), equalTo( new File[] { f5 } ) );
    }

    @Test
    public void testGetExistingFilesListCollectionOfString()
    {
        assertThat( PathUtil.getExistingFilesList( (List<String>) null ), nullValue() );
        assertThat( PathUtil.getExistingFilesList( Collections.singletonList( f5.getAbsolutePath() ) ), hasItem( f5 ) );
    }

    @Test
    public void testGetExistingFilesListListOfFile()
    {
        assertThat( PathUtil.getExistingFilesList( (List<File>) null ), nullValue() );
        assertThat( PathUtil.getExistingFilesList( Collections.singletonList( f5 ) ), hasItem( f5 ) );
        assertThat( PathUtil.getExistingFilesList( Arrays.asList( f2, f4, f5 ) ), hasItem( f5 ) );
    }

    @Test
    public void testGetFile()
        throws IOException
    {
        assertThat( PathUtil.getFile( (String) null ), nullValue() );
        assertThat( PathUtil.getFile( _PATH ), equalTo( new File( _PATH ).getCanonicalFile() ) );
    }

    @Test
    public void testGetFileFile()
    {
        assertThat( PathUtil.getFile( (File) null ), nullValue() );
        assertThat( PathUtil.getFile( f3 ), equalTo( f1 ) );
        assertThat( PathUtil.getFile( f4 ), equalTo( f2 ) );
    }

    @Test
    public void testGetFilesList()
    {
        assertThat( PathUtil.getFiles( (Collection<String>) null ), nullValue() );
        assertThat( PathUtil.getFiles( Arrays.asList( f5.getAbsolutePath() ) ), equalTo( new File[] { f5 } ) );
    }

    @Test
    public void testGetFilesListCollectionOfString()
    {
        assertThat( PathUtil.getFilesList( (Collection<String>) null ), nullValue() );
        assertThat( PathUtil.getFilesList( Arrays.asList( f5.getAbsolutePath() ) ), hasItem( f5 ) );
    }

    @Test
    public void testGetFilesListOfStringFile()
    {
        assertThat( PathUtil.getFiles( (List<String>) null, f1 ), nullValue() );
        assertThat( PathUtil.getFiles( Collections.singletonList( f5.getAbsolutePath() ), f1 ), hasItem( f5 ) );
        assertThat( PathUtil.getFiles( Collections.singletonList( f5.getName() ), f5.getParentFile() ), hasItem( f5 ) );
    }

    @Test
    public void testGetFilesStringArray()
    {
        assertThat( PathUtil.getFiles( (String[]) null ), nullValue() );
        assertThat( PathUtil.getFiles( new String[] { f5.getAbsolutePath() } ), equalTo( new File[] { f5 } ) );
    }

    @Test
    public void testGetFilesStringArrayFile()
    {
        assertThat( PathUtil.getFiles( (String[]) null, f1 ), nullValue() );
        assertThat( PathUtil.getFiles( new String[] { f5.getAbsolutePath() }, f1 ), hasItem( f5 ) );
        assertThat( PathUtil.getFiles( new String[] { f5.getName() }, f5.getParentFile() ), hasItem( f5 ) );
    }

    @Test
    public void testGetFileStringFile()
    {
        assertThat( PathUtil.getFile( null, f1 ), nullValue() );
        assertThat( PathUtil.getFile( f5.getAbsolutePath(), f1 ), equalTo( f5 ) );
        assertThat( PathUtil.getFile( f5.getName(), f5.getParentFile() ), equalTo( f5 ) );
    }

    @Test
    public void testGetFileStringFileArray()
    {
        assertThat( PathUtil.getFile( null, f5, f1 ), nullValue() );
        assertThat( PathUtil.getFile( f5.getName(), f5.getParentFile(), f1 ), equalTo( f5 ) );
        assertThat( PathUtil.getFile( f5.getAbsolutePath(), f1, f1 ), equalTo( f5 ) );
    }

    @Test
    public void testGetFileStringListOfFile()
    {
        assertThat( PathUtil.getFile( null, Collections.singletonList( f5 ) ), nullValue() );
        assertThat( PathUtil.getFile( f5.getName(), Collections.singletonList( f5.getParentFile() ) ), equalTo( f5 ) );
        assertThat( PathUtil.getFile( f5.getAbsolutePath(), Collections.singletonList( f1 ) ), equalTo( f5 ) );
        assertThat( PathUtil.getFile( f5.getName(), new ArrayList<File>() ), nullValue() );
    }

    @Test
    public void testGetPath()
        throws IOException
    {
        assertThat( PathUtil.getPath( null ), nullValue() );
        assertThat( PathUtil.getPath( f1 ), equalTo( _PATH ) );
        assertThat( PathUtil.getPath( f2 ), equalTo( ABSOLUTE_PATH ) );
    }

    @Test
    public void testGetPathsCollectionOfFile()
    {
        assertThat( PathUtil.getPaths( (Collection<File>) null ), nullValue() );
        assertThat( PathUtil.getPaths( Arrays.asList( f1, f2 ) ),
                    equalTo( new String[] { _PATH, ABSOLUTE_PATH } ) );
    }

    @Test
    public void testGetPathsFileArray()
    {
        assertThat( PathUtil.getPaths( (File[]) null ), nullValue() );
        assertThat( PathUtil.getPaths().length, equalTo( 0 ) );
    }

    @Test
    public void testGetPathsList()
    {
        assertThat( PathUtil.getPathsList( null ), nullValue() );
        assertThat( PathUtil.getPathsList( new File[] { f1, f2 } ), hasItems( _PATH, ABSOLUTE_PATH ) );
    }

    @Test
    public void testGetPathString()
    {
        assertThat( PathUtil.getPathString( null ), nullValue() );
        assertThat( PathUtil.getPathString( new File[] { f1 } ), equalTo( _PATH ) );
        assertThat( PathUtil.getPathString( new File[] { f1, f2 } ), equalTo( _PATH + File.pathSeparatorChar
            + ABSOLUTE_PATH ) );
    }

    @Test
    public void testGetRelativePath()
    {
        assertThat( PathUtil.getRelativePath( new File( "/home" ), new File( "/home/velo/content" ) ),
                    equalTo( "velo/content" ) );
        assertThat( PathUtil.getRelativePath( new File( "/home/velo/content" ), new File( "/home/velo/c2" ) ),
                    equalTo( "../c2" ) );
        assertThat( PathUtil.getRelativePath( new File( "/home/velo/content" ), new File( "/home/velo2" ) ),
                    equalTo( "../../velo2" ) );
    }

}
