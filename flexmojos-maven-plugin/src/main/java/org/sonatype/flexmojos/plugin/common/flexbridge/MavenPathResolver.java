package org.sonatype.flexmojos.plugin.common.flexbridge;

import java.io.File;
import java.util.List;

import org.apache.maven.model.Resource;

import flex2.compiler.common.SinglePathResolver;
import flex2.compiler.io.LocalFile;
import flex2.compiler.io.VirtualFile;

public class MavenPathResolver
    implements SinglePathResolver
{

    private List<Resource> resources;

    public MavenPathResolver( List<Resource> resources )
    {
        this.resources = resources;
    }

    public VirtualFile resolve( String relative )
    {
        // only resolve absolute paths here
        if ( !relative.startsWith( "/" ) )
        {
            return null;
        }

        relative = relative.substring( 1 );

        for ( Resource resource : resources )
        {
            File resourceFolder = new File( resource.getDirectory() );
            File resourceFile = new File( resourceFolder, relative );
            if ( resourceFile.exists() )
            {
                return new LocalFile( resourceFile );
            }
        }

        return null;
    }
}
