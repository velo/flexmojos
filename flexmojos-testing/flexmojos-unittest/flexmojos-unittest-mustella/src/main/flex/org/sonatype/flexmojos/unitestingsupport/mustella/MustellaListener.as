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
package org.sonatype.flexmojos.unitestingsupport.mustella
{
    import flash.utils.getDefinitionByName;

    import mx.core.Application;

    import org.sonatype.flexmojos.test.report.ErrorReport;
    import org.sonatype.flexmojos.unitestingsupport.ITestApplication;
    import org.sonatype.flexmojos.unitestingsupport.SocketReporter;
    import org.sonatype.flexmojos.unitestingsupport.UnitTestRunner;

    public class MustellaListener implements UnitTestRunner
    {

        private var _socketReporter:SocketReporter;

        public function MustellaListener( socketReporter:SocketReporter = null )
        {
            this._socketReporter = socketReporter;
        }

        public function set socketReporter( socketReporter:SocketReporter ):void
        {
            this._socketReporter = socketReporter;
        }

        public function run( testApp:ITestApplication ):int
        {
            TestOutput.getInstance().addEventListener( 'result', outputWriter );

            var tests:Array = testApp.tests;

            var i:int = 0;
            for each ( var test:*in tests )
            {
		trace("Testing", test);
                var testCase:* = new test();
                if ( testCase is UnitTester )
                {
                    i++;

                    var testComp:* = getDefinitionByName( testCase.testSWF );
                    var componentUnderTest:* = new testComp();
                    testApp.componentUnderTest = componentUnderTest;
                }
            }

            return i;

        }

        public function outputWriter( e:* ):void
        {
            var message:String = e.msg;
            trace( message );
            var parts:Array = message.split( " " );

            if ( message.indexOf( "LengthOfTestcases:" ) != -1 )
            {
                _socketReporter.totalTestCount = int( parts[ 1 ] );
            }
            else if ( message.indexOf( "TestCase Start:" ) != -1 )
            {
                var name:Array = parts[ 2 ].split( "$" );
                _socketReporter.addMethod( name[ 0 ], name[ 1 ] )
            }
            else if ( message.indexOf( "RESULT:" ) != -1 )
            {

                var scriptName:String = getContent( parts[ 1 ] );
                var id:String = getContent( parts[ 2 ] );
                var result:String = getContent( parts[ 3 ] );

                if ( "pass" == result )
                {
                    trace( 'Adding success', scriptName );
                    _socketReporter.testFinished( scriptName );
                }
                else
                {
                    trace( 'Adding error', scriptName, e.msg );

                    var failure:ErrorReport = new ErrorReport();
                    failure.message = e.msg;
                    //failure.stackTrace = error.getStackTrace();

                    _socketReporter.addFailure( scriptName, id, failure );
                    _socketReporter.testFinished( scriptName );
                }
            }
            else if ( message.indexOf( "ScriptComplete:" ) != -1 )
            {
            }

        }

        private static function getContent( msg:String ):String
        {
            var content:Array = msg.split( "=" )
            if ( content.length == 2 )
            {
                return content[ 1 ];
            }

            return null;
        }
    }
}