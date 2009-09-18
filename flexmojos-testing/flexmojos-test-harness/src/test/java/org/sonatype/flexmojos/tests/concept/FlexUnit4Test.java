/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.tests.concept;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.flexmojos.test.report.TestCaseReport;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class FlexUnit4Test
    extends AbstractConceptTest
{

    @Test
    public void testFlexUnit4Example()
        throws Exception
    {
        File testDir = getProject( "/concept/flexunit4-example" );
        test( testDir, "install" );

        File target = new File( testDir, "target" );
        File sureFireReports = new File( target, "surefire-reports" );
        AssertJUnit.assertTrue( "Report folder not created!", sureFireReports.isDirectory() );

        String[] reportFiles = sureFireReports.list();
        AssertJUnit.assertEquals( "Expected for 2 files, got: " + Arrays.toString( reportFiles ), 2, reportFiles.length );

        File reportFile = new File( sureFireReports, "TEST-AnnotatedTest.addition.AnnotatedTest.xml" );
        validateReport( reportFile );
        reportFile = new File( sureFireReports, "TEST-AnnotatedTest.doIOError.AnnotatedTest.xml" );
        validateReport( reportFile );
    }

    private void validateReport( File reportFile )
        throws XmlPullParserException, IOException, FileNotFoundException
    {
        AssertJUnit.assertTrue( "Report was not created!", reportFile.isFile() );

        TestCaseReport report = new TestCaseReport( Xpp3DomBuilder.build( new FileReader( reportFile ) ) );

        AssertJUnit.assertEquals( 0, report.getErrors() );
        AssertJUnit.assertEquals( 0, report.getFailures() );
        AssertJUnit.assertEquals( 1, report.getTests() );
    }

}
