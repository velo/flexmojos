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
