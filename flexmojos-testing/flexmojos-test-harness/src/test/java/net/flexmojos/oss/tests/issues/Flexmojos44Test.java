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
import java.io.IOException;

import org.apache.maven.it.VerificationException;
import org.testng.annotations.Test;

public class Flexmojos44Test
    extends AbstractIssueTest
{

    @Test( timeOut = 360000, expectedExceptions = { VerificationException.class } )
    public void flexmojos44()
        throws VerificationException, IOException
    {
        /*
         * test template will throw an exception locking flashplayer. Flexmojos should detect that and kill flashplayer
         * automatically
         */
        File testDir = getProject( "/issues/flexmojos-44" );
        test( testDir, "install" );
    }

}
