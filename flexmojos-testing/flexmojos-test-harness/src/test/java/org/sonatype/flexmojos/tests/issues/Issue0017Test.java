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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import java.io.File;

import org.apache.maven.it.Verifier;
import org.sonatype.flexmojos.matcher.file.FileMatcher;
import org.sonatype.flexmojos.tests.AbstractFlexMojosTests;
import org.testng.annotations.Test;

public class Issue0017Test
    extends AbstractFlexMojosTests
{
    @Test
    public void issue17()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0017" );
        Verifier verifier = getVerifier( testDir );
        verifier.getCliOptions().remove( "-Dflex.coverage=true" );
        verifier.executeGoal( siteGoal() );

        File asdoc = new File( testDir, "target/site/asdoc" );
        assertThat( asdoc, FileMatcher.exists() );
        assertThat( new File( asdoc, "index.html" ), FileMatcher.exists() );
        assertThat( new File( asdoc, "main.html" ), FileMatcher.exists() );

        File coverage = new File( testDir, "target/site/coverage" );
        assertThat( coverage, FileMatcher.exists() );
        assertThat( new File( coverage, "index.html" ), FileMatcher.exists() );
        assertThat( new File( coverage, "org.sonatype.flexmojos.coverage.CoverageDataCollector.html" ), FileMatcher.exists() );
        
        assertThat( new File( testDir, "target/asdoc" ), not( FileMatcher.exists() ) );
        assertThat( new File( testDir, "target/coverage" ), not( FileMatcher.exists() ) );
    }

}
