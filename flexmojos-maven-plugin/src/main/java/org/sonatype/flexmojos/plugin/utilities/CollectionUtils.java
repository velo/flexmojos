package org.sonatype.flexmojos.plugin.utilities;

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

    public static <E> List<E> merge( E[]... arrays )
    {
        if ( arrays == null )
        {
            return null;
        }

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
            return Collections.emptyList();
        }

        return Collections.unmodifiableList( new LinkedList<E>( merged ) );
    }

}
