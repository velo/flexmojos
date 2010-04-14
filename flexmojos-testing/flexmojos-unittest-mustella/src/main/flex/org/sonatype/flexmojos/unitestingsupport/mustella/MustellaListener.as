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