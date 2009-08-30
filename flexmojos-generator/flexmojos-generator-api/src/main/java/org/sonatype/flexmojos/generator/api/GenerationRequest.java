/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
