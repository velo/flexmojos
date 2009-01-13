/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.tests.issues;


import java.io.File;

import org.sonatype.flexmojos.tests.AbstractFlexMojosTests;

public abstract class AbstractIssueTest
    extends AbstractFlexMojosTests
{

    public void testIssue( String issueNumber, String... args )
        throws Exception
    {
        File testDir = getProject( "/issues/" + issueNumber );
        test( testDir, "install", args );
    }

}
