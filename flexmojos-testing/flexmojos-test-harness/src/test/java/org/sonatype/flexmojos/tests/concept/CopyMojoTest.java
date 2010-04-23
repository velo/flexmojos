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
package org.sonatype.flexmojos.tests.concept;

import java.io.File;
import java.util.zip.ZipFile;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CopyMojoTest
    extends AbstractConceptTest
{

    @Test
    public void copyFlexResouces()
        throws Exception
    {
        File testDir = getProject( "/concept/copy-flex-resources" );
        test( testDir, "install" );

        File warFile = new File( testDir, "war/target/copy-war-1.0-SNAPSHOT.war" );
        Assert.assertTrue( warFile.exists(), "War file not found!" );

        ZipFile war = new ZipFile( warFile );
        Assert.assertNotNull( war.getEntry( "copy-swf-1.0-SNAPSHOT.swf" ), "Swf entry not present at war!" );
        Assert.assertNotNull( war.getEntry( "copy-swf-1.0-SNAPSHOT-module1.swf" ), "Swf entry not present at war!" );
        Assert.assertNotNull( war.getEntry( "copy-swf-1.0-SNAPSHOT-module2.swf" ), "Swf entry not present at war!" );
        Assert.assertNotNull( war.getEntry( "rsls/framework-3.2.0.3958.swf" ), "Rsl entry not present at war!" );
    }

}
