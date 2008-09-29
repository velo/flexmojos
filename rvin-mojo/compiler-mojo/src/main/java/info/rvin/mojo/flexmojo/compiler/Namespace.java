package info.rvin.mojo.flexmojo.compiler;

import java.io.File;

public class Namespace
{

    private File manifest;

    private String uri;

    public Namespace()
    {
        super();
    }

    public Namespace( String uri, File manifest )
    {
        super();
        this.uri = uri;
        this.manifest = manifest;
    }

    public File getManifest()
    {
        return manifest;
    }

    public String getUri()
    {
        return uri;
    }

    public void setManifest( File manifest )
    {
        this.manifest = manifest;
    }

    public void setUri( String uri )
    {
        this.uri = uri;
    }
}
