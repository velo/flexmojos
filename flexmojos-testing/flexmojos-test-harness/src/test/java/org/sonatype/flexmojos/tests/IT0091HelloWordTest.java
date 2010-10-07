/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.tests;

import java.io.File;

import org.hamcrest.MatcherAssert;
import org.sonatype.flexmojos.matcher.file.FileMatcher;
import org.sonatype.flexmojos.test.FMVerifier;
import org.testng.annotations.Test;

public class IT0091HelloWordTest
    extends AbstractFlexMojosTests
{

    @Test
    public void helloWordTest()
        throws Exception
    {
        FMVerifier v = test( getProject( "intro/hello-world" ), "install", "-DisIt=true" );
        String dir = v.getBasedir();

        File target = new File( dir, "target" );
        File main = new File( target, "hello-world-1.0-SNAPSHOT.swf" );

        assertSeftExit( main, 3539,v );
    }

    @Test
    public void helloWordNoInherit()
        throws Exception
    {
        FMVerifier v = test( getProject( "intro/hello-world-no-inherit" ), "install", "-DisIt=true" );
        String dir = v.getBasedir();

        File target = new File( dir, "target" );
        File main = new File( target, "hello-world-no-inherit-1.0-SNAPSHOT.swf" );

        assertSeftExit( main, 3539, v );
    }

    @Test
    public void asdoc()
        throws Exception
    {
        String dir = test( getProject( "intro/hello-world-no-inherit" ), "flexmojos:asdoc" ).getBasedir();

        File target = new File( dir, "target" );
        File asdoc = new File( target, "asdoc" );

        MatcherAssert.assertThat( asdoc, FileMatcher.exists() );
    }

}
