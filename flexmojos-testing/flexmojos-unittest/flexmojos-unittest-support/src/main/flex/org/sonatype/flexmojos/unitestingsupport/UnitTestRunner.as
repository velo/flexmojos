/**
 * @author Marvin Froeder
 */
package org.sonatype.flexmojos.unitestingsupport
{

    public interface UnitTestRunner
    {

        function run( testApp:ITestApplication ):int;

        function set socketReporter( socketReporter:SocketReporter ):void;

    }

}