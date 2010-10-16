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
package org.sonatype.flexmojos.tests.issues;

import static org.testng.Assert.fail;

import org.apache.maven.it.VerificationException;
import org.sonatype.flexmojos.test.FMVerifier;
import org.sonatype.flexmojos.tests.AbstractFlexMojosTests;
import org.testng.annotations.Test;

public class Flexmojos2Test
    extends AbstractFlexMojosTests
{

    @Test
    public void allFilesUsed()
        throws Exception
    {
        FMVerifier v = test( getProject( "intro/hello-world" ), "install", "-Dflex.failIfUnused=true" );
        v.verifyTextInLog( "All files included" );
    }

    @Test
    public void notAllUsed()
        throws Exception
    {
        FMVerifier v = getVerifier( getProject( "issues/flexmojos-2/simple" ) );
        v.getCliOptions().add( "-Dflex.failIfUnused=true" );
        try
        {
            v.executeGoal( "install" );
            fail( "there was supposed to be unsued files" );
        }
        catch ( VerificationException e )
        {
            v.verifyTextInLog( "Some files were not included on the build:" );
            v.verifyTextInLog( "module.mxml" );
        }
    }

    @Test
    public void modules()
        throws Exception
    {
        FMVerifier v = test( getProject( "issues/flexmojos-2/modules" ), "install", "-Dflex.failIfUnused=true" );
        v.verifyTextInLog( "All files included" );
    }

}
