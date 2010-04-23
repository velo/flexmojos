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
package org.sonatype.flexmojos.tests.coverage;

import static org.testng.AssertJUnit.assertTrue;

import java.io.File;

import org.testng.annotations.Test;

public class HtmlWrapperTemplateTest
    extends AbstractCoverageTest
{

    @Test
    public void testHtmlwrapperTemplates()
        throws Exception
    {
        File testDir = getProject( "/coverage/htmlwrapper-templates" );
        test( testDir, "package" );
        File folder = new File( testDir, "folder/target/htmlwrapper-templates-folder-1.0-SNAPSHOT.html" );
        assertTrue( "Should generate htmlwrapper" + folder.getAbsolutePath(), folder.exists() );
        File zip = new File( testDir, "zip/target/htmlwrapper-templates-zip-1.0-SNAPSHOT.html" );
        assertTrue( "Should generate htmlwrapper " + zip.getAbsolutePath(), zip.exists() );
    }

}
