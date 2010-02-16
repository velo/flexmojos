package org.sonatype.flexmojos.common.matcher;

import org.apache.maven.artifact.Artifact;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

class ClassifierMatcher
    extends TypeSafeMatcher<Artifact>
{

    private String classifier;

    ClassifierMatcher( String classifier )
    {
        if ( classifier == null )
        {
            throw new IllegalArgumentException( "classifier must be not null" );
        }
        this.classifier = classifier;
    }

    @Override
    public boolean matchesSafely( Artifact a )
    {
        return classifier.equals( a.getClassifier() );
    }

    public void describeTo( Description msg )
    {
        msg.appendText( " classifier " );
        msg.appendValue( classifier );
    }

}
