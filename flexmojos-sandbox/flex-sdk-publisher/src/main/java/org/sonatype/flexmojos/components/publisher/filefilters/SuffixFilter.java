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
