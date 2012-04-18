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
package net.flexmojos.oss.plugin.compiler.attributes;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.FileSet;

public class SimplifiablePattern
{

    private List<String> includes;

    private List<FileSet> patterns;

    public void addInclude( String include )
    {
        getIncludes().add( include );
    }

    public void addScan( FileSet pattern )
    {
        getPatterns().add( pattern );
    }

    public List<String> getIncludes()
    {
        if ( includes == null )
        {
            includes = new ArrayList<String>();
        }
        return includes;
    }

    public List<FileSet> getPatterns()
    {
        if ( patterns == null )
        {
            patterns = new ArrayList<FileSet>();
        }
        return patterns;
    }

    public void setIncludes( List<String> includes )
    {
        this.includes = includes;
    }

    public void setPatterns( List<FileSet> patterns )
    {
        this.patterns = patterns;
    }

}
