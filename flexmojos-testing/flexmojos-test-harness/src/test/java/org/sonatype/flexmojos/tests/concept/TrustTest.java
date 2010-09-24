/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
