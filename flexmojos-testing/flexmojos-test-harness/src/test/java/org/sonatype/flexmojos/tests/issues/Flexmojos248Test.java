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
package org.sonatype.flexmojos.tests.issues;

import java.io.File;

import org.hamcrest.MatcherAssert;
import org.sonatype.flexmojos.matcher.file.FileMatcher;
import org.sonatype.flexmojos.test.FMVerifier;
import org.testng.annotations.Test;

public class Flexmojos248Test
    extends AbstractIssueTest
{

    @Test
    public void moduleFiles()
        throws Exception
    {
        FMVerifier v = testIssue( "flexmojos-248", "-DloadExternsOnModules=true" );
        String dir = v.getBasedir();
        validateCompilation( dir, v );
    }

    @Test
    public void moduleFilesLoadExternsOnModules()
        throws Exception
    {
        FMVerifier v = testIssue( "flexmojos-248", "-DloadExternsOnModules=false" );
        String dir = v.getBasedir();
        validateCompilation( dir, v );
    }

    @Test
    public void multiplePoms()
        throws Exception
    {
        File testDir = getProject( "/issues/flexmojos-248", "pom.xml", "m.xml", "p1.xml", "p2.xml" );
        FMVerifier v = test( testDir, "install", "-f", "m.xml" );
        String dir = v.getBasedir();
        test( testDir, "install", "-f", "p1.xml" );
        test( testDir, "install", "-f", "p2.xml" );
        validateCompilation( dir, v );
    }

    private void validateCompilation( String dir, FMVerifier v )
        throws Exception
    {
        File target = new File( dir, "target" );
        File main = new File( target, "test-flex-modules-0.0.1-SNAPSHOT.swf" );
        MatcherAssert.assertThat( main, FileMatcher.isFile() );

        File module1 = new File( target, "test-flex-modules-0.0.1-SNAPSHOT-module1.swf" );
        MatcherAssert.assertThat( module1, FileMatcher.isFile() );
        File module2 = new File( target, "test-flex-modules-0.0.1-SNAPSHOT-module1.swf" );
        MatcherAssert.assertThat( module2, FileMatcher.isFile() );

        assertSeftExit( main, 3539, v );
    }
}
