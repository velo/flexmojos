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
package org.sonatype.flexmojos.test.report
{

    import mx.collections.ArrayCollection;

    import org.sonatype.flexmojos.coverage.CoverageDataCollector;

    [Bindable]
    [RemoteClass( alias="org.sonatype.flexmojos.test.report.TestCaseReport" )]
    public class TestCaseReport extends TestCaseReportBase
    {

        public function TestCaseReport()
        {
            methods = new ArrayCollection();
        }

        /*
         * Return the method Object from the internal TestSuite model for the
         * currently executing method on a Test.
         * @param test the Test.
         * @return the method Object.
         */
        public function getMethod( methodName:String ):TestMethodReport
        {
            var method:TestMethodReport;
            for each ( method in methods )
            {
                if ( method.name == methodName )
                {
                    return method;
                }
            }

            method = new TestMethodReport();
            method.name = methodName;
            method.time = 0;

            methods.addItem( method );

            return method;
        }

        /*
           <?xml version="1.0" encoding="UTF-8" ?>
           <testsuite errors="0" skipped="0" tests="1" time="0.312" failures="0" name="com.Test">
           <testcase classname="flex.Test" time="0.297" name="testExecute"/>
           <testcase classname="flex.Test" time="0.297" name="testExecute"/>
           <testcase time="3.125" name="removeAllSnapshots">
           <failure message="All artifacts should be deleted by SnapshotRemoverTask." type="junit.framework.AssertionFailedError">
           junit.framework.AssertionFailedError: All artifacts should be deleted by SnapshotRemoverTask. Found: [H:\home_hudson\.hudson\workspace\Nexus\jdk\1.6\label\windows\trunk\nexus\nexus-test-harness\nexus-test-harness-launcher\target\bundle\nexus-webapp-1.2.0-SNAPSHOT\runtime\work\storage\nexus-test-harness-snapshot-repo\nexus634\artifact\1.0-SNAPSHOT\artifact-1.0-20010101.184024-1.jar, H:\home_hudson\.hudson\workspace\Nexus\jdk\1.6\label\windows\trunk\nexus\nexus-test-harness\nexus-test-harness-launcher\target\bundle\nexus-webapp-1.2.0-SNAPSHOT\runtime\work\storage\nexus-test-harness-snapshot-repo\nexus634\artifact\1.0-SNAPSHOT\artifact-1.0-SNAPSHOT.jar]
           at junit.framework.Assert.fail(Assert.java:47)
           at junit.framework.Assert.assertTrue(Assert.java:20)
           </failure>
           <system-out>[INFO] Nexus configuration validated succesfully.
           </system-out>
           </testcase>
           </testsuite>
         */
        public function toXml():String
        {
            var genxml:String = "<testsuite errors='"+ errors +  
                    "' failures='"+failures+
                    "' name='" + name.replace( "::", "." ) +
                    "' tests='"+tests +
                    "' time='"+ time + "' >";

            for each ( var methodReport:TestMethodReport in methods )
            {
                genxml +=  methodReport.toXml().toXMLString();
            }

            var data:Object = CoverageDataCollector.extractCoverageResult();
            for ( var cls:String in data )
            {
                genxml +=  TestCoverageReport(data[cls]).toXml() ;
            }
            
            genxml += "</testsuite>"
            return genxml;
        }

    }
}