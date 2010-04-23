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
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Flexmojos52Test
    extends AbstractIssueTest
{

    @Test
    public void flexmojos52()
        throws Exception
    {
        File testDir = getProject( "/issues/flexmojos-52" );
        test( testDir, "install" );

        File target = new File( testDir, "target" );
        File acOet = new File( target, "AC_OETags.js" );
        checkFile( acOet );
        File index = new File( target, "flexmojos-52-1.0-SNAPSHOT.html" );
        checkFile( index );
    }

    private void checkFile( File file )
        throws IOException
    {
        Assert.assertTrue( file.exists() );
        String content = FileUtils.fileRead( file );
        Assert.assertFalse( content.contains( "${application}" ) );
        Assert.assertTrue( content.contains( "flexmojos-52" ) );
    }

}
