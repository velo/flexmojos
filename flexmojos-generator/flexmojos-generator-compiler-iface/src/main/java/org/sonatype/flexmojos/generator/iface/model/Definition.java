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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias( "def" )
public class Definition
{

    private Class<?> classname;

    @XStreamImplicit( itemFieldName = "method" )
    private Set<MethodSignature> methods;

    public Definition()
    {
        super();
    }

    public Definition( Class<?> classname, Collection<MethodSignature> methods )
    {
        this();
        this.classname = classname;
        this.methods = new LinkedHashSet<MethodSignature>( methods );
    }

    public Definition( Class<?> classname, MethodSignature... methods )
    {
        this( classname, Arrays.asList( methods ) );
    }

    public Class<?> getClassname()
    {
        return classname;
    }

    public void setClassname( Class<?> classname )
    {
        this.classname = classname;
    }

    public Set<MethodSignature> getMethods()
    {
        if ( this.methods == null )
        {
            this.methods = new LinkedHashSet<MethodSignature>();
        }
        return methods;
    }

    public void setMethods( Collection<MethodSignature> methods )
    {
        this.methods = new LinkedHashSet<MethodSignature>( methods );
    }

}
