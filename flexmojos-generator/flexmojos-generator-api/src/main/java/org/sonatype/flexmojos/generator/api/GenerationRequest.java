package org.sonatype.flexmojos.generator.api;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class GenerationRequest
{

    private Map<String, File> classes;

    private ClassLoader classLoader;

    // UidFieldName
    // OutputEnumToBaseOutputDirectory
    // UsingTideEntity
    private Map<String, String> extraOptions;

    private File persistentOutputFolder;

    private Map<String, String> templates;

    private File transientOutputFolder;
    
    private String[] translators;

    public String[] getTranslators() {
		return translators;
	}

	public void setTranslators(String[] translators) {
		this.translators = translators;
	}

	public void addClass( String classname, File sourceJar )
    {
        getClasses().put( classname, sourceJar );
    }

    public Map<String, File> getClasses()
    {
        if ( classes == null )
        {
            classes = new LinkedHashMap<String, File>();
        }
        return classes;
    }

    public ClassLoader getClassLoader()
    {
        return this.classLoader;
    }

    public Map<String, String> getExtraOptions()
    {
        if ( extraOptions == null )
        {
            this.extraOptions = new LinkedHashMap<String, String>();
        }
        return extraOptions;
    }

    public File getPersistentOutputFolder()
    {
        return persistentOutputFolder;
    }

    public Map<String, String> getTemplates()
    {
        return templates;
    }

    public File getTransientOutputFolder()
    {
        return transientOutputFolder;
    }

    public void setClasses( Map<String, File> classes )
    {
        this.classes = classes;
    }

    public void setClassLoader( ClassLoader classLoader )
    {
        this.classLoader = classLoader;
    }

    public void setExtraOptions( Map<String, String> extraOptions )
    {
        this.extraOptions = extraOptions;
    }

    public void setPersistentOutputFolder( File persistentOutputFolder )
    {
        this.persistentOutputFolder = persistentOutputFolder;
    }

    public void setTemplates( Map<String, String> templates )
    {
        this.templates = templates;
    }

    public void setTransientOutputFolder( File transientOutputFolder )
    {
        this.transientOutputFolder = transientOutputFolder;
    }

    public void addExtraOption( String name, String value )
    {
        getExtraOptions().put( name, value );
    }

}
