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
