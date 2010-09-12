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
