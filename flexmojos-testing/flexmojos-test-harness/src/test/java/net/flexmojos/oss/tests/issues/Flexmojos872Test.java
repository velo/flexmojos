/**
 * Flexmojos is a set of maven goals to allow maven users to compile,
 * optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
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

import net.flexmojos.oss.test.report.TestCaseReport;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

/**
 * There was a problem with Flexmojos' FlexUnit integration, if a
 * FlexUnit version greater than 4.1 was used. As this allowed to
 * use parametrized tests resulting in one test function being
 * executed multiple times. Before the fix Flexmojos stopped listening
 * for results and sent back the report as soon as the number of
 * tests was executed that matched the number of test functions hereby
 * disguising any test failures beyond that. The solution was to
 * make the test-listeners decide when to finish.
 */
public class Flexmojos872Test
    extends AbstractIssueTest
{

    @Test
    public void testParametrizedTestsExecution()
        throws Exception
    {
        File testDir = getProject( "/issues/flexmojos-872" );
        test( testDir, "install", "-Dmaven.test.failure.ignore=true" );

        File target = new File( testDir, "target" );
        File sureFireReports = new File( target, "surefire-reports" );
        AssertJUnit.assertTrue("Report folder not created!", sureFireReports.isDirectory());

        String[] reportFiles = sureFireReports.list();
        AssertJUnit.assertEquals( "Expected for 3 files, got: " + Arrays.toString(reportFiles), 3, reportFiles.length );

        // This test consists of one test function with a dataProvider that causes the test function to be
        // executed 100 times. All of these should be successful.
        File reportFile = new File( sureFireReports, "TEST-unittest.ParametrizedTestWithoutFailuresTest.xml" );
        validateReport( reportFile, 100, 0, 0 );

        // This test consists of one test function with a dataProvider that causes the test function to be
        // executed 100 times. One of these should fail.
        reportFile = new File( sureFireReports, "TEST-unittest.ParametrizedTestWithFailuresTest.xml" );
        validateReport( reportFile, 100, 0, 1 );

        // This test consists of one test function with a dataProvider that causes the test function to be
        // executed 100 times. One of these should produce an error.
        reportFile = new File( sureFireReports, "TEST-unittest.ParametrizedTestWithErrorsTest.xml" );
        validateReport( reportFile, 100, 0, 1 );

    }

    private void validateReport( File reportFile, int expectedNumTests, int expectedNumErrors, int expectedNumFailures )
        throws XmlPullParserException, IOException
    {
        AssertJUnit.assertTrue( "Report was not created!", reportFile.isFile() );

        TestCaseReport report = new TestCaseReport( Xpp3DomBuilder.build(new FileReader(reportFile)) );

        System.out.println("Report: Tests " + report.getTests() + " Errors " + report.getErrors() + " Failures " + report.getFailures());

        AssertJUnit.assertEquals( expectedNumErrors, report.getErrors() );
        AssertJUnit.assertEquals( expectedNumFailures, report.getFailures() );
        AssertJUnit.assertEquals( expectedNumTests, report.getTests() );
    }

}
