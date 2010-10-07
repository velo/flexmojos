/**
 * Copyright 2008 Marvin Herman Froeder
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.tests.issues;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import java.io.File;

import org.sonatype.flexmojos.matcher.file.FileMatcher;
import org.sonatype.flexmojos.test.FMVerifier;
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
        FMVerifier verifier = getVerifier( testDir );
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
