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
package net.flexmojos.oss.tests.issues;

import java.io.File;

import org.apache.maven.it.VerificationException;
import net.flexmojos.oss.test.FMVerifier;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Flexmojos141Test
    extends AbstractIssueTest
{

    /**
     * This test is disabled because the check had to be disabled in order to have
     * Flexmojos working with Falcon.
     *
     * @throws Exception
     */
    @Test(enabled = false)
    public void testInvalidVersion()
        throws Exception
    {
        File testDir = getProject( "/issues/" + "flexmojos-141" );
        FMVerifier verifier = getVerifier( testDir );
        try
        {
            verifier.executeGoal( "install" );
            Assert.fail();
        }
        catch ( VerificationException e )
        {
            // expected
        }

        verifier.verifyTextInLog( "Flex compiler and flex framework versions doesn't match." );
    }

}
