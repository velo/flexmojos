package org.sonatype.flexmojos.plugin.compiler.attributes.converter;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.FileSet;

public class SimplifiablePattern
{

    private List<String> includes;

    private List<FileSet> patterns;

    public void addInclude( String include )
    {
        getIncludes().add( include );
    }

    public void addPattern( FileSet pattern )
    {
        getPatterns().add( pattern );
    }

    public List<String> getIncludes()
    {
        if ( includes == null )
        {
            includes = new ArrayList<String>();
        }
        return includes;
    }

    public List<FileSet> getPatterns()
    {
        if ( patterns == null )
        {
            patterns = new ArrayList<FileSet>();
        }
        return patterns;
    }

    public void setIncludes( List<String> includes )
    {
        this.includes = includes;
    }

    public void setPatterns( List<FileSet> patterns )
    {
        this.patterns = patterns;
    }

}
