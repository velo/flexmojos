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
package net.flexmojos.oss.tests.concept;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.io.IOException;

import org.apache.maven.it.VerificationException;
import org.hamcrest.Matcher;
import net.flexmojos.oss.test.FMVerifier;
import net.flexmojos.oss.tests.AbstractFlexMojosTests;
import net.flexmojos.oss.tests.matcher.ClassMatcher;
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
