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
package org.sonatype.flexmojos.plugin.compiler.flexbridge;

import java.io.File;
import java.util.List;

import org.apache.maven.model.Resource;

import flex2.compiler.common.SinglePathResolver;
import flex2.compiler.io.LocalFile;
import flex2.compiler.io.VirtualFile;

public class MavenPathResolver
    implements SinglePathResolver
{

    private List<Resource> resources;

    public MavenPathResolver( List<Resource> resources )
    {
        this.resources = resources;
    }

    public VirtualFile resolve( String relative )
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
                return new LocalFile( resourceFile );
            }
        }

        return null;
    }
}
