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
package org.sonatype.flexmojos.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class CollectionUtils
{

    public static <E> List<E> merge( Collection<E>... cols )
    {
        if ( cols == null )
        {
            return null;
        }

        Set<E> merged = new LinkedHashSet<E>();
        for ( Collection<E> col : cols )
        {
            if ( col == null || col.isEmpty() )
            {
                continue;
            }

            merged.addAll( col );
        }

        if ( merged.isEmpty() )
        {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList( new LinkedList<E>( merged ) );
    }

    @SuppressWarnings( "unchecked" )
    public static <E> E[] merge( E[]... arrays )
    {
        if ( arrays == null )
        {
            return null;
        }

        Class<E> clazz = (Class<E>) arrays.getClass().getComponentType().getComponentType();

        Set<E> merged = new LinkedHashSet<E>();
        for ( E[] es : arrays )
        {
            if ( es == null || es.length == 0 )
            {
                continue;
            }

            merged.addAll( Arrays.asList( es ) );
        }

        if ( merged.isEmpty() )
        {
            return (E[]) Array.newInstance( clazz, 0 );
        }

        return merged.toArray( (E[]) Array.newInstance( clazz, merged.size() ) );
    }

}
