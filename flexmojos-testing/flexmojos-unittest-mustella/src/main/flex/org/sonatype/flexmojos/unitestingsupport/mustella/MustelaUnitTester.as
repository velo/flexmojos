package org.sonatype.flexmojos.unitestingsupport.mustella
{
    import org.sonatype.flexmojos.unitestingsupport.UnitTestRunner;
    import org.sonatype.flexmojos.unitestingsupport.util.ClassnameUtil;


    public class MustelaUnitTester extends UnitTester
    {

        public function MustelaUnitTester()
        {
        }

        public function set testClass( v:Class ):void
        {
            if ( v == null )
            {
                super.testSWF = null;
            }
            else
            {
                var o:* = new v();
                super.testSWF = ClassnameUtil.getClassName( o );
            }
        }

        override public function stringToObject( s:* ):Object
        {
            var cheatString:String;
            if ( s == null || s == "" )
            {
                cheatString = "componentUnderTest";
            }
            else
            {
                cheatString = "componentUnderTest." + s;
            }

            var obj:* = super.stringToObject( cheatString );
            trace( "Cheating resolve: '" + cheatString + "' got '" + obj + "'" );
            if ( obj != null )
            {
                return obj;
            }

            trace( "Trying to resolve: '" + s + "' got '" + obj + "'" );
            obj = super.stringToObject( s );

            return obj;
        }

    }
}