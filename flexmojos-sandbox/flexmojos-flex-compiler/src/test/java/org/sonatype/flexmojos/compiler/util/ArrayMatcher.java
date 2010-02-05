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
package org.sonatype.flexmojos.compiler.util;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class ArrayMatcher
{

    public static <E> Matcher<List<E>> subArray( final E... subArray )
    {
        if ( subArray == null || subArray.length == 0 )
        {
            throw new AssertionError( "Invalid subarray: " + Arrays.toString( subArray ) );
        }
        return new TypeSafeMatcher<List<E>>()
        {
            @Override
            public boolean matchesSafely( List<E> list )
            {
                E first = subArray[0];
                if ( !list.contains( first ) )
                {
                    return false;
                }

                for ( int i = 0; i < list.size(); i++ )
                {
                    E e = list.get( i );
                    if ( ( first == null && e == null ) || first == e || first.equals( e ) )
                    {
                        if ( list.size() < i + subArray.length )
                        {
                            return false;
                        }
                        List<E> subList = list.subList( i, i + subArray.length );
                        //if doesn't match keep looking
                        if(subList.equals( Arrays.asList( subArray ) )) {
                            return true;
                        }
                    }
                }
                return false;
            }

            public void describeTo( Description description )
            {
                description.appendText( "a collection containing this exact sub array " ).appendValue( subArray );
            }
        };
    }

}
