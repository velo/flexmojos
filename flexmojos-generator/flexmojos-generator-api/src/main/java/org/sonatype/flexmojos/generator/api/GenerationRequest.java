package org.sonatype.flexmojos.generator.api;

import java.io.File;
import java.util.Map;

public class GenerationRequest
{

    // UidFieldName
    // OutputEnumToBaseOutputDirectory
    // UsingTideEntity
    private Map<String, String> extraOptions;

    private File transientOutputFolder;

    private File persistentOutputFolder;

    private Map<String, File> classes;

    private Map<String, String> templates;

    private ClassLoader classLoader;

    public void setClassLoader( ClassLoader classLoader )
    {
        this.classLoader = classLoader;
    }

    public Map<String, String> getExtraOptions()
    {
        return extraOptions;
    }

    public void setExtraOptions( Map<String, String> extraOptions )
    {
        this.extraOptions = extraOptions;
    }

    public File getTransientOutputFolder()
    {
        return transientOutputFolder;
    }

    public void setTransientOutputFolder( File transientOutputFolder )
    {
        this.transientOutputFolder = transientOutputFolder;
    }

    public File getPersistentOutputFolder()
    {
        return persistentOutputFolder;
    }

    public void setPersistentOutputFolder( File persistentOutputFolder )
    {
        this.persistentOutputFolder = persistentOutputFolder;
    }

    public Map<String, File> getClasses()
    {
        return classes;
    }

    public void setClasses( Map<String, File> classes )
    {
        this.classes = classes;
    }

    public Map<String, String> getTemplates()
    {
        return templates;
    }

    public void setTemplates( Map<String, String> templates )
    {
        this.templates = templates;
    }

    public ClassLoader getClassLoader()
    {
        return this.classLoader;
    }

}
