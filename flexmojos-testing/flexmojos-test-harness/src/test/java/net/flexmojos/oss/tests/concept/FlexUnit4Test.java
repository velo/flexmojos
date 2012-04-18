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
package net.flexmojos.oss.tests.concept;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import net.flexmojos.oss.test.report.TestCaseReport;
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
