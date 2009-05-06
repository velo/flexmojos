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
package org.sonatype.flexmojos.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;

public class FileInterpolationUtil
{
    public static void copyDirectory( File from, File dest, Map<String, String> variables,
                                      String[] excludesInterpolation )
        throws IOException
    {
        dest.mkdirs();

        DirectoryScanner scan = new DirectoryScanner();
        scan.setBasedir( from );
        scan.setExcludes( excludesInterpolation );
        scan.addDefaultExcludes();
        scan.scan();

        String[] files = scan.getIncludedFiles();
        for ( String fileName : files )
        {
            File sourceFile = new File( from, fileName );
            File destFile = new File( dest, fileName );
            copyFile( sourceFile, destFile, variables );
        }

        scan = new DirectoryScanner();
        scan.setBasedir( from );
        scan.setIncludes( excludesInterpolation );
        scan.addDefaultExcludes();
        scan.scan();

        files = scan.getIncludedFiles();
        for ( String fileName : files )
        {
            File sourceFile = new File( from, fileName );
            File destFile = new File( dest, fileName );
            FileUtils.copyFile( sourceFile, destFile );
        }

    }

    public static void copyFile( File sourceFile, File destFile, Map<String, String> variables )
        throws FileNotFoundException, IOException
    {
        // does destinations directory exist ?
        if ( destFile.getParentFile() != null && !destFile.getParentFile().exists() )
        {
            destFile.getParentFile().mkdirs();
        }

        FileReader reader = null;
        FileWriter writer = null;
        try
        {
            reader = new FileReader( sourceFile );
            InterpolationFilterReader filterReader = new InterpolationFilterReader( reader, variables );

            writer = new FileWriter( destFile );

            IOUtil.copy( filterReader, writer );
        }
        finally
        {
            IOUtil.close( reader );
            IOUtil.close( writer );
        }
    }

}
