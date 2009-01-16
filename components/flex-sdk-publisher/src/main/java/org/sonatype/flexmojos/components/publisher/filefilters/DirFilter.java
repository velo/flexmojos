package org.sonatype.flexmojos.components.publisher.filefilters;

import java.io.File;
import java.io.FileFilter;

public class DirFilter
    implements FileFilter
{

    public static final FileFilter INSTANCE = new DirFilter();;

    private DirFilter()
    {
        super();
    }

    public boolean accept( File pathname )
    {
        return pathname.isDirectory();
    }

}
