/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.generator;

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

    private GeneratorLogger logger;

    private File persistentOutputFolder;

    private Map<String, String> templates;

    private File transientOutputFolder;

    private String[] translators;

    protected void addClass( String classname, File sourceJar )
    {
        getClasses().put( classname, sourceJar );
    }

    protected void addExtraOption( String name, String value )
    {
        getExtraOptions().put( name, value );
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

    public GeneratorLogger getLogger()
    {
        return logger;
    }

    public File getPersistentOutputFolder()
    {
        return persistentOutputFolder;
    }

    public Map<String, String> getTemplates()
    {
        if ( templates == null )
        {
            templates = new LinkedHashMap<String, String>();
        }
        return templates;
    }

    public File getTransientOutputFolder()
    {
        return transientOutputFolder;
    }

    public String[] getTranslators()
    {
        return translators;
    }

    protected void setClasses( Map<String, File> classes )
    {
        this.classes = classes;
    }

    protected void setClassLoader( ClassLoader classLoader )
    {
        this.classLoader = classLoader;
    }

    protected void setExtraOptions( Map<String, String> extraOptions )
    {
        this.extraOptions = extraOptions;
    }

    protected void setLogger( GeneratorLogger logger )
    {
        this.logger = logger;
    }

    protected void setPersistentOutputFolder( File persistentOutputFolder )
    {
        this.persistentOutputFolder = persistentOutputFolder;
    }

    protected void setTemplates( Map<String, String> templates )
    {
        this.templates = templates;
    }

    protected void setTransientOutputFolder( File transientOutputFolder )
    {
        this.transientOutputFolder = transientOutputFolder;
    }

    protected void setTranslators( String[] translators )
    {
        this.translators = translators;
    }

}
