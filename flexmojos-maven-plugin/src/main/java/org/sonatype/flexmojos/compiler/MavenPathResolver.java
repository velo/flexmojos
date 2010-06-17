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
package org.sonatype.flexmojos.compiler;

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
            File resourceFolder = new File( resource.getTargetPath() );
            File resourceFile = new File( resourceFolder, relative );
            if ( resourceFile.exists() )
            {
                return resourceFile;
            }
        }

        return null;
    }

}
