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

import org.testng.annotations.Test;

public class Issue0076Test
    extends AbstractIssueTest
{

    @Test
    public void notDeclared()
        throws Exception
    {
        // will not generate bundle and will not merge it
        super.testIssue( "issue-0076" );
    }

    @Test
    public void merged()
        throws Exception
    {
        super.testIssue( "issue-0076", "-Pmerge-true" );
    }

    @Test
    public void notMerged()
        throws Exception
    {
        super.testIssue( "issue-0076", "-Pmerge-false" );
    }

}
