/**
 * Copyright 2008 Marvin Herman Froeder
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.tests.issues;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.io.FileReader;

import org.apache.maven.it.VerificationException;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.sonatype.flexmojos.test.report.TestCaseReport;
import org.sonatype.flexmojos.tests.AbstractFlexMojosTests;
import org.testng.annotations.Test;

public class Issue0015Test
    extends AbstractFlexMojosTests
{

    @Test( expectedExceptions = { VerificationException.class } )
    public void issue15()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0015" );
        try
        {
            test( testDir, "install" );
        }
        finally
        {
            File reportDir = new File( testDir, "target/surefire-reports" );
            assertEquals( 2, reportDir.listFiles().length );

            File reportFile = new File( reportDir, "TEST-com.adobe.example.TestCalculator.xml" );
            TestCaseReport report = new TestCaseReport( Xpp3DomBuilder.build( new FileReader( reportFile ) ) );

            assertEquals( "com.adobe.example.TestCalculator", report.getName() );
            assertEquals( 2, report.getTests() );
            assertEquals( 1, report.getErrors() );
        }

    }
}
