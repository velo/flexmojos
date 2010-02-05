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
