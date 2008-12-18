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
package info.flexmojos.tests.concept;

import info.flexmojos.compile.test.report.TestCaseReport;
import info.rvin.flexmojo.test.util.XStreamFactory;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.junit.Assert;
import org.testng.annotations.Test;

public class FlexUnitTest
    extends AbstractConceptTest
{

    @Test( timeOut = 120000, expectedExceptions = { VerificationException.class } )
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
