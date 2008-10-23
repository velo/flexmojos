/*
 * Flex-mojos is a set of maven plugins to allow maven users to compile, optimize, test and ... Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008 Marvin Herman Froeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
