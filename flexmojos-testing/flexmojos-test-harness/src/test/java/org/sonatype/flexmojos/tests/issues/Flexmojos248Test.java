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
package org.sonatype.flexmojos.tests.issues;

import java.io.File;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.sonatype.flexmojos.tests.FileMatcher;
import org.testng.annotations.Test;

public class Flexmojos248Test
    extends AbstractIssueTest
{

    @Test( timeOut = 60000 )
    public void moduleFiles()
        throws Exception
    {
        String dir = testIssue( "flexmojos-248", "-DloadExternsOnModules=true" ).getBasedir();
        validateCompilation( dir );
    }

    @Test( timeOut = 60000 )
    public void moduleFilesLoadExternsOnModules()
        throws Exception
    {
        String dir = testIssue( "flexmojos-248", "-DloadExternsOnModules=false" ).getBasedir();
        validateCompilation( dir );
    }

    @Test( timeOut = 240000 )
    public void multiplePoms()
        throws Exception
    {
        File testDir = getProject( "/issues/flexmojos-248", "pom.xml", "m.xml", "p1.xml", "p2.xml" );
        String dir = test( testDir, "install", "-f m.xml" ).getBasedir();
        test( testDir, "install", "-f p1.xml" ).getBasedir();
        test( testDir, "install", "-f p2.xml" ).getBasedir();
        validateCompilation( dir );
    }

    private void validateCompilation( String dir )
        throws Exception
    {
        File target = new File( dir, "target" );
        File main = new File( target, "test-flex-modules-0.0.1-SNAPSHOT.swf" );
        MatcherAssert.assertThat( main, FileMatcher.isFile() );

        File module1 = new File( target, "test-flex-modules-0.0.1-SNAPSHOT-module1.swf" );
        MatcherAssert.assertThat( module1, FileMatcher.isFile() );
        File module2 = new File( target, "test-flex-modules-0.0.1-SNAPSHOT-module1.swf" );
        MatcherAssert.assertThat( module2, FileMatcher.isFile() );

        Process p = null;
        try
        {
            p = Runtime.getRuntime().exec( new String[] { "flashplayer", main.getCanonicalPath() } );
            MatcherAssert.assertThat( p.waitFor(), CoreMatchers.equalTo( 0 ) );
        }
        finally
        {
            p.destroy();
        }
    }
}
