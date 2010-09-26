package org.sonatype.flexmojos.unitestingsupport
{

    public interface ITestApplication
    {
        function get tests():Array;

        function get componentUnderTest():*;

        function set componentUnderTest( cmp:* ):void;

        function addTest( test:Class ):void;

        function killApplication():void;
    }
}