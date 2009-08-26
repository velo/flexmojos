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

import java.io.File;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.sonatype.flexmojos.test.report.TestCaseReport;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class FluintTest
    extends AbstractConceptTest
{

    @Test
    public void passFluint()
        throws Exception
    {
        Verifier v = standardConceptTester( "/fluint-example/pass" );
        File testDir = new File( v.getBasedir() );

        TestCaseReport report = getTestReport( testDir, "fluint.ExampleTest" );

        AssertJUnit.assertEquals( 0, report.getErrors() );
        AssertJUnit.assertEquals( 0, report.getFailures() );
        AssertJUnit.assertEquals( 1, report.getTests() );
    }

    @Test( expectedExceptions = { VerificationException.class } )
    public void failFluint()
        throws Exception
    {
        File testDir = getProject( "/concept/fluint-example/fail" );
        try
        {
            test( testDir, "install" );
        }
        finally
        {
            TestCaseReport report = getTestReport( testDir, "fluint.ExampleTest" );

            AssertJUnit.assertEquals( 0, report.getErrors() );
            AssertJUnit.assertEquals( 1, report.getFailures() );
            AssertJUnit.assertEquals( 1, report.getTests() );
        }
    }

}
