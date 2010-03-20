package org.sonatype.flexmojos.matcher.collection;

import java.util.Collection;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class CollectionsMatcher
{

    public static Matcher<Collection<?>> isSize( final int i )
    {
        return new TypeSafeMatcher<Collection<?>>()
        {
            public boolean matchesSafely( Collection<?> item )
            {
                return item != null && item.size() == i;
            }

            public void describeTo( Description description )
            {
                description.appendText( " collection has " );
                description.appendValue( i );
                description.appendText( " items " );
            }
        };
    }

}
