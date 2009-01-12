/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package info.flexmojos.test.util;

import info.flexmojos.test.report.ErrorReport;
import info.flexmojos.test.report.TestCaseReport;
import info.flexmojos.test.report.TestMethodReport;

import com.thoughtworks.xstream.XStream;

public class XStreamFactory
{
    public static XStream getXStreamInstance()
    {
        XStream xs = new XStream();
        xs.processAnnotations( TestCaseReport.class );
        xs.processAnnotations( TestMethodReport.class );
        xs.processAnnotations( ErrorReport.class );
        return xs;
    }

}
