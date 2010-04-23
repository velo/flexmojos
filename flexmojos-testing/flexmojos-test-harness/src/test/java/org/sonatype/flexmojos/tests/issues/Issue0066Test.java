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

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;

import org.testng.annotations.Test;

public class Issue0066Test
    extends AbstractIssueTest
{

    @Test( groups = { "generator" } )
    public void issue66()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0066" );
        test( testDir, "install" );

        // Issue 62 test
        // is excluded!
        File another = new File( testDir, "flex/src/main/flex/org/sonatype/flexmojos/generator/AnotherPojo.as" );
        assertFalse( "File not found " + another, another.isFile() );

        // Issue 65 test
        File pojo = new File( testDir, "flex/src/main/flex/org/sonatype/flexmojos/generator/SimplePojo.as" );
        assertTrue( "File not found " + pojo, pojo.isFile() );
        File base =
            new File( testDir,
                      "flex/target/generated-sources/flexmojos/org/sonatype/flexmojos/generator/SimplePojoBase.as" );
        assertTrue( "File not found " + base, base.isFile() );
    }

}
