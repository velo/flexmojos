package org.sonatype.flexmojos.generator;

import java.io.File;
import java.util.Map;

public class TestGenerationRequest
    extends GenerationRequest
{
    @Override
    public void addClass( String classname, File sourceJar )
    {
        super.addClass( classname, sourceJar );
    }

    @Override
    public void addExtraOption( String name, String value )
    {
        super.addExtraOption( name, value );
    }

    @Override
    public void setClasses( Map<String, File> classes )
    {
        super.setClasses( classes );
    }

    @Override
    public void setExtraOptions( Map<String, String> extraOptions )
    {
        super.setExtraOptions( extraOptions );
    }

    @Override
    public void setClassLoader( ClassLoader classLoader )
    {
        super.setClassLoader( classLoader );
    }

    @Override
    public void setLogger( GeneratorLogger logger )
    {
        super.setLogger( logger );
    }

    @Override
    public void setPersistentOutputFolder( File persistentOutputFolder )
    {
        super.setPersistentOutputFolder( persistentOutputFolder );
    }

    @Override
    public void setTemplates( Map<String, String> templates )
    {
        super.setTemplates( templates );
    }

    @Override
    public void setTransientOutputFolder( File transientOutputFolder )
    {
        super.setTransientOutputFolder( transientOutputFolder );
    }

    @Override
    public void setTranslators( String[] translators )
    {
        super.setTranslators( translators );
    }

}
