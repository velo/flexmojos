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

import org.sonatype.flexmojos.test.FMVerifier;
import org.testng.annotations.Test;

public class Flexmojos315Test
    extends AbstractIssueTest
{

    @Test
    public void resourcesEmbedding()
        throws Exception
    {
        FMVerifier v = test( getProject( "issues/flexmojos-315" ), "install" );
        String dir = v.getBasedir();

        File target = new File( dir, "target" );
        File main = new File( target, "flexmojos-315-1.0-SNAPSHOT.swf" );

        assertSeftExit( main, 3539, v );
    }

}
