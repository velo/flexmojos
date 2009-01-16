/**
 * Copyright 2008 Marvin Herman Froeder
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.components.publisher.filefilters;

import java.io.File;
import java.io.FileFilter;

public class SuffixFilter
    implements FileFilter
{

    private String[] extensions;

    public SuffixFilter( String... extension )
    {
        super();
        if ( extension == null )
        {
            throw new NullPointerException( "Invalid null extension" );
        }
        this.extensions = extension;
    }

    public boolean accept( File pathname )
    {
        for ( String extension : extensions )
        {
            if ( pathname.getName().endsWith( extension ) )
            {
                return true;
            }
        }
        return false;
    }

}
