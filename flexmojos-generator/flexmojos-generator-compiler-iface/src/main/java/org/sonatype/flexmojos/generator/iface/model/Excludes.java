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
package org.sonatype.flexmojos.generator.iface.model;

import java.util.Arrays;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias( Excludes.NAME )
public class Excludes
{

    public static final String NAME = "excludes";

    @XStreamImplicit( itemFieldName = "exclude" )
    private List<Definition> excludes;

    public Excludes()
    {
        super();
    }

    public Excludes( List<Definition> excludes )
    {
        this();
        this.excludes = excludes;
    }

    public Excludes( Definition... excludes )
    {
        this( Arrays.asList( excludes ) );
    }

    public List<Definition> getExcludes()
    {
        return excludes;
    }

    public void setIgnores( List<Definition> excludes )
    {
        this.excludes = excludes;
    }

}
