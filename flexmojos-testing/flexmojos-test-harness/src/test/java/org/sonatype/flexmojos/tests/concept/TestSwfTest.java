package org.sonatype.flexmojos.tests.concept;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.io.IOException;

import org.apache.maven.it.VerificationException;
import org.hamcrest.Matcher;
import org.sonatype.flexmojos.test.FMVerifier;
import org.sonatype.flexmojos.tests.AbstractFlexMojosTests;
import org.sonatype.flexmojos.tests.matcher.ClassMatcher;
import org.testng.annotations.Test;

public class TestSwfTest
    extends AbstractFlexMojosTests
{

    @SuppressWarnings( "all" )
    private static final Matcher<File> TEST_CLASS = ClassMatcher.hasClass( "flexunit.framework:TestSuite" );

    @Test
    public void testSwf()
        throws Exception
    {
        File main = compile( "flexmojos:compile-swf", "hello-world-1.0-SNAPSHOT.swf" );
        assertThat( main, not( TEST_CLASS ) );

        main = compile( "flexmojos:test-swf", "hello-world-1.0-SNAPSHOT-test.swf" );
        assertThat( main, TEST_CLASS );
    }

    private File compile( String goal, String file )
        throws VerificationException, IOException, Exception
    {
        FMVerifier v = test( getProject( "intro/hello-world" ), goal, "-DisIt=true" );
        String dir = v.getBasedir();

        File target = new File( dir, "target" );
        File main = new File( target, file );

        assertSeftExit( main, 3539, v );
        return main;
    }
}
