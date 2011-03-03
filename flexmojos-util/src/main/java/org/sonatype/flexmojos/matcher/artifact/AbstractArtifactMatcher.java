package org.sonatype.flexmojos.matcher.artifact;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

abstract class AbstractArtifactMatcher<E>
    extends TypeSafeMatcher<E>
{
    private String element;

    private Matcher<? extends String> elementMatcher;

    public AbstractArtifactMatcher( Matcher<? extends String> elementMatcher, String element )
    {
        this.elementMatcher = elementMatcher;
        this.element = element;
    }

    public void describeTo( Description description )
    {
        description.appendText( "a dependency with " + element + " " ).appendDescriptionOf( elementMatcher );
    }

    protected abstract String getValue( E item );

    @Override
    public boolean matchesSafely( E item )
    {
        return elementMatcher.matches( getValue( item ) );
    }
}