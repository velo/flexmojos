package org.sonatype.flexmojos.matcher.artifact;

import org.apache.maven.artifact.Artifact;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

class ClassifierMatcher
    extends TypeSafeMatcher<Artifact>
{

    private String classifier;

    ClassifierMatcher( String classifier )
    {
        this.classifier = classifier;
    }

    @Override
    public boolean matchesSafely( Artifact a )
    {
        if ( classifier == null )
        {
            return a.getClassifier() == null;
        }
        return classifier.equals( a.getClassifier() );
    }

    public void describeTo( Description msg )
    {
        msg.appendText( " classifier " );
        msg.appendValue( classifier );
    }

}
