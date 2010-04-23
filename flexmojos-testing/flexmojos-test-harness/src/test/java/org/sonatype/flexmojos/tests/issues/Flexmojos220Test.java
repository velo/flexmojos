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

import junit.framework.Assert;

import org.testng.annotations.Test;

public class Flexmojos220Test
    extends AbstractIssueTest
{

    @Test
    public void localizedModules()
        throws Exception
    {
        String baseDir = testIssue( "flexmojos-220" ).getBasedir();
        File main = new File( baseDir, "target/flexmojos-220-1.0-SNAPSHOT.swf" );
        Assert.assertTrue( main.exists() );
        File module = new File( baseDir, "target/flexmojos-220-1.0-SNAPSHOT-module.swf" );
        Assert.assertTrue( module.exists() );
        File locale = new File( baseDir, "target/locales/flexmojos-220-1.0-SNAPSHOT-en_US.swf" );
        Assert.assertTrue( locale.exists() );
        File report = new File( baseDir, "target/flexmojos-220-1.0-SNAPSHOT-link-report.xml" );
        Assert.assertTrue( report.exists() );
    }

}
