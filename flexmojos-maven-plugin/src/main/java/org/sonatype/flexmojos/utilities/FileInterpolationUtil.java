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
    /**
     * This list defines a series of common binary file extensions that should be 
     * excluded by default for copyDirectory in addition to those added by
     * {@link DirectoryScanner#addDefaultExcludes()}.  Since we are performing text
     * replacement, these should not be scanned unless the user explicitly says so.
     */
    private static final String[] binaryExcludes = 
    {
        // Common adobe binaries
        "**/*.swf",
        "**/*.swc",
        "**/*.swz",
        "**/*.flv",
        "**/*.fla",
        "**/*.pdf",
        "**/*.ps",
        "**/*.eps",
        
        // Images
        "**/*.png",
        "**/*.jpg",
        "**/*.jpeg",
        "**/*.jif",
        "**/*.jiff",
        "**/*.bmp,",
        "**/*.gif,",
        "**/*.tif",
        "**/*.tiff",
        
        // Audio
        "**/*.wav",
        "**/*.mp3",
        "**/*.aac",
        "**/*.m4a",
        "**/*.mid",
        "**/*.midi",
        "**/*.mpa",
        "**/*.ra",
        "**/*.ram",
        "**/*.wma",
        
        // Video
        "**/*.avi",
        "**/*.mov",
        "**/*.mp4",
        "**/*.mpg",
        "**/*.mpeg",
        "**/*.qt",
        "**/*.rm",
        "**/*.wmv",
        
        // Fonts
        "**/*.fnt",
        "**/*.fon",
        "**/*.otf",
        "**/*.ttf",
        
        // Compressed
        "**/*.zip",
        "**/*.rar",
        "**/*.tar",
        "**/*.tar.gz",
        "**/*.gz",
        "**/*.7z",
        
        // Documentation
        "**/*.doc",
        "**/*.docx",
        "**/*.ppt",
        "**/*.pptx",
        "**/*.xls",
        "**/*.xlsx",
        "**/*.odt",
        "**/*.ods",
        "**/*.odp",
        
        // Other
        "**/*.exe"
    };
    
    /**
     * Provided to mirror the method signature and behavior of the old
     * implementation.
     */
    public static void copyDirectory( File from, File dest, Map<String, String> variables,
                                      String[] excludesInterpolation )
        throws IOException
    {
        copyDirectory( from, dest, variables, excludesInterpolation, null, false );
    }
    
    public static void copyDirectory( File from, File dest, Map<String, String> variables,
                                      String[] excludesInterpolation, String[] includesInterpolation,
                                      boolean useDefaultExcludes )
        throws IOException
    {
        dest.mkdirs();

        DirectoryScanner scan = new DirectoryScanner();
        scan.setBasedir( from );
        
        // Add default binary excludes unless told otherwise
        if( useDefaultExcludes )
        {
            excludesInterpolation = addDefaultExcludes( excludesInterpolation );
        }
        
        scan.setExcludes( excludesInterpolation );
        scan.setIncludes( includesInterpolation );
        
        // Excludes things like svn, cvs, or temp files
        scan.addDefaultExcludes();
        
        scan.scan();
        
        // Interpolated copy for included files
        for ( String fileName : scan.getIncludedFiles() )
        {
            File sourceFile = new File( from, fileName );
            File destFile = new File( dest, fileName );
            copyFile( sourceFile, destFile, variables );
        }
        
        // Plain copy for not-included and excluded files 
        for ( String fileName : scan.getNotIncludedFiles() )
        {
            File sourceFile = new File( from, fileName );
            File destFile = new File( dest, fileName );
            FileUtils.copyFile( sourceFile, destFile );
        }
        
        for ( String fileName : scan.getExcludedFiles() )
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
    
    /**
     * Copied and modified from private {@link DirectoryScanner#addDefaultExcludes()} method.
     * @param excludes is the current exclusion list
     * @return merger of the provided excludes and the default binary excludes
     */
    private static String[] addDefaultExcludes(String[] excludes)
    {
        int excludesLength = excludes == null ? 0 : excludes.length;
        String[] newExcludes;
        newExcludes = new String[excludesLength + binaryExcludes.length];
        if ( excludesLength > 0 )
        {
            System.arraycopy( excludes, 0, newExcludes, 0, excludesLength );
        }
        for ( int i = 0; i < binaryExcludes.length; i++ )
        {
            newExcludes[i + excludesLength] = binaryExcludes[i].replace( '/',
                                                                          File.separatorChar ).replace( '\\', File.separatorChar );
        }
        
        return newExcludes;
    }

}
