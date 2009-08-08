package org.sonatype.flexmojos.tests.issues;

import org.apache.maven.it.VerificationException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.text.StringContains;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Flexmojos159Test
    extends AbstractIssueTest
{

    @Test
    public void localizationFromLibrary()
        throws Exception
    {
        try
        {
            testIssue( "flexmojos-159" );
            Assert.fail();
        }
        catch ( VerificationException e )
        {
            MatcherAssert.assertThat(
                                      e.getMessage(),
                                      StringContains.containsString( "Unable to resolve resource bundle \"TestBundle\" for locale \"en_US\"." ) );
            return;
        }
    }

}
