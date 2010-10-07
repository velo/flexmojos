/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.tests.concept;

import java.io.File;

import org.apache.maven.it.VerificationException;
import org.sonatype.flexmojos.test.FMVerifier;
import org.sonatype.flexmojos.test.report.TestCaseReport;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class FluintUnitTest
    extends AbstractConceptTest
{

    @Test
    public void passFluint()
        throws Exception
    {
        FMVerifier v = standardConceptTester( "/fluint-example/pass" );
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
