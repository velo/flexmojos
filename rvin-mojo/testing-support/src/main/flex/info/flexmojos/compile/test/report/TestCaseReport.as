/*
 * Flex-mojos is a set of maven plugins to allow maven users to compile, optimize, test and ... Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008 Marvin Herman Froeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.flexmojos.compile.test.report {

    [Bindable]
    [RemoteClass(alias="info.flexmojos.compile.test.report.TestCaseReport")]
    public class TestCaseReport extends TestCaseReportBase {

		public function TestCaseReport()
		{
			methods = new Array();
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
			for each (method in methods) 
			{
				if(method.name == methodName)
				{
					return method;
				}
			}
			
			method = new TestMethodReport(); 
			method.name = methodName;
			
			methods.push(method);
			
			return method;
		}

		/*
			<testCaseReport>
			  <errors>2</errors>
			  <failures>2</failures>
			  <methods>
			    <testMethodReport>
			      <error>
			        <message>message</message>
			        <stackTrace>test</stackTrace>
			        <type>my.error.1</type>
			      </error>
			      <failure>
			        <message>message</message>
			        <stackTrace>test</stackTrace>
			        <type>my.error.1</type>
			      </failure>
			      <name>testMethod</name>
			      <time>25.3</time>
			    </testMethodReport>
			  </methods>
			  <name>info.flexmojos.Test1</name>
			  <tests>10</tests>
			  <time>323.5</time>
			</testCaseReport>
		 */
		public function toXml():XML {
			
			var methodsXml:XML = <methods></methods>;
			for each (var methodReport:TestMethodReport in methods)
			{
				methodsXml = methodsXml.appendChild(methodReport.toXml());
			}
			
			var xml:XML =
				<testCaseReport>
					<errors> { errors } </errors>
					<failures> { failures } </failures>
					{ methodsXml }
					<name> { name } </name>
					<tests> { tests } </tests>
					<time> { time } </time>
				</testCaseReport>;
				
			return xml;
		}

    }
}