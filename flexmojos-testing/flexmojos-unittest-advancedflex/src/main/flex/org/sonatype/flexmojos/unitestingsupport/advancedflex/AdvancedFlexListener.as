/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.unitestingsupport.advancedflex
{
    import advancedflex.debugger.aut.framework.*;

    import org.sonatype.flexmojos.unitestingsupport.ITestApplication;
    import org.sonatype.flexmojos.unitestingsupport.SocketReporter;
    import org.sonatype.flexmojos.unitestingsupport.UnitTestRunner;
    import org.sonatype.flexmojos.unitestingsupport.util.ClassnameUtil;

    public class AdvancedFlexListener implements UnitTestRunner
    {

        private var _socketReporter:SocketReporter;

        public function set socketReporter( socketReporter:SocketReporter ):void
        {
            this._socketReporter = socketReporter;
        }

        public function run( testApp:ITestApplication ):int
        {
            var tests:Array = testApp.tests;

            var suite:TestSuite = new TestSuite();

            var testsCount:int = 0;

            for each ( var test:Class in tests )
            {
                var testCase:* = new test();
                if ( testCase is TestCase )
                {
                    suite.addTest( testCase );
                    testsCount++;
                }
            }

            //suite will crash if launched without tests
            //http://code.google.com/p/advancedflex/issues/detail?id=1
            if ( testsCount != 0 )
            {
                suite.startTest( new ProtectedConsole( ClassnameUtil.getClassName( test ), _socketReporter ) );
            }

            return testsCount;
        }

    }
}