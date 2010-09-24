/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.tests.concept;

import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.FileReader;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.flexmojos.truster.FlashPlayerTruster;
import org.testng.annotations.Test;

public class TrustTest
    extends AbstractConceptTest
{

    @Test
    public void testTrust()
        throws Exception
    {
        File fpTrustFolder = container.lookup( FlashPlayerTruster.class ).getTrustDir();

        if ( !fpTrustFolder.isDirectory() )
        {
            fpTrustFolder.mkdirs();
        }

        File mavenCfg = new File( fpTrustFolder, "maven.cfg" );
        if ( mavenCfg.exists() )
        {
            mavenCfg.delete();
        }

        String basedir = standardConceptTester( "flashplayertrust" ).getBasedir();

        String cfg = IOUtil.toString( new FileReader( mavenCfg ) );

        assertTrue( "flexmojos should write trust localtion", cfg.contains( basedir ) );
    }

}
