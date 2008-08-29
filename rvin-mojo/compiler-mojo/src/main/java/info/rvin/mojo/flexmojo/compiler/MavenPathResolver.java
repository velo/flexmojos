package info.rvin.mojo.flexmojo.compiler;

import java.io.File;
import java.util.List;

import org.apache.maven.model.Resource;

import flex2.tools.oem.PathResolver;

public class MavenPathResolver
    implements PathResolver
{

    private List<Resource> resources;

    public MavenPathResolver( List<Resource> resources )
    {
        this.resources = resources;
    }

    public File resolve( String relative )
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
                return resourceFile;
            }
        }

        return null;
    }

}
