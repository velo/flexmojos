/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * this class provides functions used to generate a relative path from two absolute paths
 * 
 * @author Marvin H. Froeder
 * @author David M. Howard
 */
public class PathUtil
{

    private PathUtil()
    {
        super();
    }

    static
    {
        new PathUtil();
    }

    public static boolean existAll( File... files )
    {
        if ( files == null )
        {
            return false;
        }
        if ( files.length == 0 )
        {
            return false;
        }

        for ( File file : files )
        {
            if ( !file.exists() )
            {
                return false;
            }
        }
        return true;
    }

    public static boolean existAll( List<String> paths )
    {
        if ( paths == null )
        {
            return false;
        }

        return existAll( getFiles( paths ) );
    }

    public static boolean existAny( File... files )
    {
        if ( files == null )
        {
            return false;
        }
        if ( files.length == 0 )
        {
            return false;
        }

        for ( File file : files )
        {
            if ( file.exists() )
            {
                return true;
            }
        }
        return false;
    }

    public static File getFile( File file )
    {
        if ( file == null )
        {
            return null;
        }

        try
        {
            return file.getCanonicalFile();
        }
        catch ( IOException e )
        {
            return file.getAbsoluteFile();
        }
    }

    public static File getFile( String path, File basedir )
    {
        if ( path == null )
        {
            return null;
        }

        File file = new File( path );
        if ( !file.isAbsolute() )
        {
            file = new File( basedir, path );
        }

        return getFile( file );
    }

    public static File getFile( String path, File... basedirs )
    {
        if ( path == null )
        {
            return null;
        }

        return getFile( path, Arrays.asList( basedirs ) );
    }

    public static File getFile( String path, List<File> basedirs )
    {
        if ( path == null )
        {
            return null;
        }

        File file = new File( path );

        if ( file.isAbsolute() )
        {
            return file;
        }

        for ( File basedir : basedirs )
        {
            file = getFile( path, basedir );
            if ( file.exists() )
            {
                return file;
            }
        }

        return null;
    }

    public static Collection<File> getFiles( List<String> paths, File basedir )
    {
        if ( paths == null )
        {
            return null;
        }

        List<File> files = new ArrayList<File>();
        for ( String path : paths )
        {
            files.add( getFile( path, basedir ) );
        }

        return files;
    }

    public static Collection<File> getFiles( String[] paths, File basedir )
    {
        if ( paths == null )
        {
            return null;
        }

        return getFiles( Arrays.asList( paths ), basedir );
    }

    public static String getPath( File file )
    {
        if ( file == null )
        {
            return null;
        }

        try
        {
            return file.getCanonicalPath();
        }
        catch ( IOException e )
        {
            return file.getAbsolutePath();
        }
    }

    public static String[] getPaths( Collection<File> files )
    {
        if ( files == null )
        {
            return null;
        }

        return getPaths( files.toArray( new File[files.size()] ) );
    }

    public static String[] getPaths( File... files )
    {
        if ( files == null )
        {
            return null;
        }

        String[] paths = new String[files.length];
        for ( int i = 0; i < paths.length; i++ )
        {
            paths[i] = getPath( files[i] );
        }
        return paths;
    }

    public static List<String> getPathsList( File[] files )
    {
        if ( files == null )
        {
            return null;
        }
        return Arrays.asList( getPaths( files ) );
    }

    public static String getPathString( File[] files )
    {
        if ( files == null )
        {
            return null;
        }

        StringBuilder paths = new StringBuilder();
        for ( File file : files )
        {
            if ( paths.length() != 0 )
            {
                paths.append( File.pathSeparatorChar );
            }
            paths.append( getPath( file ) );
        }
        return paths.toString();
    }

    public static File[] getExistingFiles( Collection<String> paths )
    {
        if ( paths == null )
        {
            return null;
        }

        return getExistingFilesList( paths ).toArray( new File[0] );
    }

    public static File[] getExistingFiles( File... files )
    {
        if ( files == null )
        {
            return null;
        }

        return getExistingFilesList( Arrays.asList( files ) ).toArray( new File[0] );
    }

    public static List<File> getExistingFilesList( Collection<String> paths )
    {
        if ( paths == null )
        {
            return null;
        }

        return getExistingFilesList( getFilesList( paths ) );
    }

    public static List<File> getExistingFilesList( List<File> files )
    {
        if ( files == null )
        {
            return null;
        }

        files = new ArrayList<File>( files );
        for ( Iterator<File> iterator = files.iterator(); iterator.hasNext(); )
        {
            File file = (File) iterator.next();
            if ( !file.exists() )
            {
                iterator.remove();
            }
        }

        return files;
    }

    public static File getFile( String path )
    {
        if ( path == null )
        {
            return null;
        }

        return getFile( new File( path ) );
    }

    public static File[] getFiles( Collection<String> paths )
    {
        if ( paths == null )
        {
            return null;
        }

        File[] files = new File[paths.size()];
        int i = 0;
        for ( String path : paths )
        {
            files[i++] = getFile( new File( path ) );
        }

        return files;
    }

    public static File[] getFiles( String... paths )
    {
        if ( paths == null )
        {
            return null;
        }

        return getFiles( Arrays.asList( paths ) );
    }

    public static List<File> getFilesList( Collection<String> paths )
    {
        if ( paths == null )
        {
            return null;
        }

        return Arrays.asList( getFiles( paths ) );
    }

    /**
     * break a path down into individual elements and add to a list. example : if a path is /a/b/c/d.txt, the breakdown
     * will be [d.txt,c,b,a]
     * 
     * @param f input file
     * @return a List collection with the individual elements of the path in reverse order
     */
    private static List<String> getPathList( File f )
    {
        List<String> l = new ArrayList<String>();
        File r = getFile( f );
        while ( r != null )
        {
            l.add( r.getName() );
            r = r.getParentFile();
        }
        return l;
    }

    /**
     * get relative path of File 'f' with respect to 'home' directory example : home = /a/b/c f = /a/d/e/x.txt s =
     * getRelativePath(home,f) = ../../d/e/x.txt
     * 
     * @param home base path, should be a directory, not a file, or it doesn't make sense
     * @param f file to generate path for
     * @return path from home to f as a string
     */
    public static String getRelativePath( File home, File f )
    {
        List<String> homelist = getPathList( home );
        List<String> filelist = getPathList( f );
        return matchPathLists( homelist, filelist ).replace( '\\', '/' );
    }

    /**
     * figure out a string representing the relative path of 'f' with respect to 'r'
     * 
     * @param r home path
     * @param f path of file
     */
    private static String matchPathLists( List<String> r, List<String> f )
    {
        int i;
        int j;
        String s;
        // start at the beginning of the lists
        // iterate while both lists are equal
        s = "";
        i = r.size() - 1;
        j = f.size() - 1;

        // first eliminate common root
        while ( ( i >= 0 ) && ( j >= 0 ) && ( r.get( i ).equals( f.get( j ) ) ) )
        {
            i--;
            j--;
        }

        // for each remaining level in the home path, add a ..
        for ( ; i >= 0; i-- )
        {
            s += ".." + File.separator;
        }

        // for each level in the file path, add the path
        for ( ; j >= 1; j-- )
        {
            s += f.get( j ) + File.separator;
        }

        // file name
        s += f.get( j );
        return s;
    }

    public static String getFileExtention( File file )
    {
        if ( file == null )
        {
            return null;
        }

        String path = file.getName();

        String[] doted = path.split( "\\." );
        if ( doted.length == 1 )
        {
            return "";
        }

        if ( "gz".equals( doted[doted.length - 1] ) || "bz2".equals( doted[doted.length - 1] ) )
        {
            if ( doted.length > 2 && "tar".equals( doted[doted.length - 2].toLowerCase() ) )
            {
                return "tar." + doted[doted.length - 1];
            }
        }

        return doted[doted.length - 1];
    }

}
