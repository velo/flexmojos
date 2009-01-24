/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.tests.concept;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.junit.Assert;
import org.sonatype.flexmojos.test.report.TestCaseReport;
import org.sonatype.flexmojos.test.util.XStreamFactory;
import org.testng.annotations.Test;

public class FlexUnitTest
    extends AbstractConceptTest
{

    @Test( expectedExceptions = { VerificationException.class } )
    public void testFlexUnitExample()
        throws Exception
    {
        File testDir = getProject( "/concept/flexunit-example/" );
        try
        {
            test( testDir, "install" );
        }
        finally
        {
            File sureFireReports = new File( testDir, "target/surefire-reports" );
            Assert.assertTrue( "Report folder not created!", sureFireReports.isDirectory() );

            String[] reportFiles = sureFireReports.list();
            Assert.assertEquals( "Expected for 2 files, got: " + Arrays.toString( reportFiles ), 2, reportFiles.length );

            File reportFile = new File( sureFireReports, "TEST-com.adobe.example.TestCalculator.xml" );
            Assert.assertTrue( "Report was not created!", reportFile.isFile() );

            String reportContent = FileUtils.readFileToString( reportFile );
            TestCaseReport report = (TestCaseReport) XStreamFactory.getXStreamInstance().fromXML( reportContent );

            Assert.assertEquals( 1, report.getErrors() );
            Assert.assertEquals( 1, report.getFailures() );
            Assert.assertEquals( 3, report.getTests() );
        }
    }

}
