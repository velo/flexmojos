package org.sonatype.flexmojos.plugin.utilities;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.sonatype.flexmojos.util.PathUtil;

public class SourceFileResolver
{

    /**
     * Resolve a source file in the source paths
     * 
     * @param project maven project
     * @param sourceFileName sugested name on pom
     * @return source file or null if source not found
     */
    public static File resolveSourceFile( List<String> sourcePaths, String sourceFileName, String groupId,
                                          String artifactId )
    {
        return resolveSourceFile( sourceFileName, PathUtil.filesList( sourcePaths ), groupId, artifactId );
    }

    public static File resolveSourceFile( List<String> sourcePaths, String sourceFile )
    {
        return resolveSourceFile( sourceFile, PathUtil.filesList( sourcePaths ) );
    }

    public static File resolveSourceFile( String sourceFileName, List<File> sourceRoots )
    {
        File absoluteSourceFile = new File( sourceFileName );
        if ( absoluteSourceFile.isAbsolute() )
        {
            return absoluteSourceFile;
        }

        for ( File sourceDirectory : sourceRoots )
        {
            File sourceFile = new File( sourceDirectory, sourceFileName );
            if ( !sourceFile.exists() )
            {
                continue;
            }
            return sourceFile;
        }

        throw new IllegalArgumentException( "Source file '" + sourceFileName + "' not found at source roots: "
            + sourceRoots );
    }

    /**
     * Resolve a source file in the source paths
     * 
     * @param project maven project
     * @param sourceFileName sugested name on pom
     * @return source file or null if source not found
     */
    public static File resolveSourceFile( String sourceFileName, List<File> sourceRoots, String groupId,
                                          String artifactId )
    {

        if ( sourceFileName != null )
        {
            return resolveSourceFile( sourceFileName, sourceRoots );
        }

        // TODO Source file was not defined, flexmojos will guess one.

        for ( File sourceDirectory : sourceRoots )
        {
            File sourceFile = resolveFile( sourceDirectory, artifactId );
            if ( sourceFile == null )
            {
                sourceFile = resolveSourceFileByGroupIdAndArtifactId( sourceDirectory, groupId, artifactId );
            }

            if ( sourceFile != null )
            {
                return sourceFile;
            }
        }

        throw new IllegalArgumentException(
                                            "SourceFile not specified and no default found!\nhttp://repository.sonatype.org/content/sites/flexmojos-site/"
                                                + MavenUtils.getFlexMojosVersion()
                                                + "/compile-swf-mojo.html#sourceFile" );
    }

    private static File resolveFile( File sourceDirectory, String artifactId )
    {
        if ( !sourceDirectory.isDirectory() )
        {
            return null;
        }

        File[] files = sourceDirectory.listFiles( new FileFilter()
        {
            public boolean accept( File pathname )
            {
                return pathname.isFile()
                    && ( pathname.getName().endsWith( ".mxml" ) || pathname.getName().endsWith( ".as" ) || pathname.getName().endsWith( ".css" ) );
            }
        } );

        if ( files.length == 1 )
        {
            return files[0];
        }
        if ( files.length > 1 )
        {
            for ( File file : files )
            {
                if ( file.getName().equalsIgnoreCase( "Main.mxml" ) || file.getName().equalsIgnoreCase( "Main.as" ) )
                {
                    return file;
                }
            }

            for ( File file : files )
            {
                if ( file.getName().equalsIgnoreCase( "Index.mxml" ) || file.getName().equalsIgnoreCase( "Index.as" ) )
                {
                    return file;
                }
            }

            if ( artifactId != null )
            {
                for ( File file : files )
                {
                    if ( file.getName().equalsIgnoreCase( artifactId + ".mxml" )
                        || file.getName().equalsIgnoreCase( artifactId + ".as" ) )
                    {
                        return file;
                    }
                }
            }

            List<File> appFiles = new ArrayList<File>();
            for ( File file : files )
            {
                if ( file.getName().endsWith( ".mxml" ) && isApplicationFile( file ) )
                {
                    appFiles.add( file );
                }
            }

            if ( appFiles.size() == 1 )
            {
                File file = appFiles.get( 0 );
                return file;
            }

        }

        return null;
    }

    private static File resolveSourceFileByGroupIdAndArtifactId( File sourceDirectory, String groupId, String artifactId )
    {
        if ( groupId == null )
        {
            return null;
        }

        String localPath = groupId.replace( '.', File.separatorChar );

        File packageDirectory = new File( sourceDirectory, localPath );
        if ( !packageDirectory.isDirectory() )
        {
            // there is no dir, so no file =D
            return null;
        }

        // let's try just groupId
        File sourceFile = resolveFile( packageDirectory, artifactId );
        if ( sourceFile != null )
        {
            return sourceFile;
        }

        if ( groupId.endsWith( artifactId ) )
        {
            return null;
        }

        packageDirectory = new File( packageDirectory, artifactId );

        sourceFile = resolveFile( packageDirectory, artifactId );

        return sourceFile;
    }

    /**
     * Parse an MXML file and returns true if the file is an application one
     * 
     * @param file the file to be parsed
     * @return true if the file is an application one
     */
    private static boolean isApplicationFile( File file )
    {
        try
        {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.getXMLReader().setFeature( "http://xml.org/sax/features/namespaces", true );
            parser.getXMLReader().setFeature( "http://xml.org/sax/features/namespace-prefixes", true );
            ApplicationHandler h = new ApplicationHandler();
            parser.parse( file, h );
            return h.isApplicationFile();
        }
        catch ( Exception e )
        {
            return false;
        }
    }

}
