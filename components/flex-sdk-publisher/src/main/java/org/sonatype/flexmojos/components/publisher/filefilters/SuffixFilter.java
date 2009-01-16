package org.sonatype.flexmojos.components.publisher.filefilters;

import java.io.File;
import java.io.FileFilter;

public class SuffixFilter
    implements FileFilter
{

    private String[] extensions;

    public SuffixFilter( String... extension )
    {
        super();
        if ( extension == null )
        {
            throw new NullPointerException( "Invalid null extension" );
        }
        this.extensions = extension;
    }

    public boolean accept( File pathname )
    {
        for ( String extension : extensions )
        {
            if ( pathname.getName().endsWith( extension ) )
            {
                return true;
            }
        }
        return false;
    }

}
