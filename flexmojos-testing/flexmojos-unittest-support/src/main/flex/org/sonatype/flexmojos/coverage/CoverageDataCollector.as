package org.sonatype.flexmojos.coverage
{
    import org.sonatype.flexmojos.test.report.TestCoverageReport;

    public class CoverageDataCollector
    {
        public function CoverageDataCollector()
        {
        }

        private static var map:Object = new Object();

        public static function collect( classname:String, lineNumber:int ):void
        {
            var data:TestCoverageReport;
            if ( ( data = map[ classname ] ) == null )
            {
                data = new TestCoverageReport( classname );
                map[ classname ] = data;
            }

            data.touch( lineNumber );
        }

        public static function extractCoverageResult():Object
        {
            var result:Object = map;
            map = new Object();
            return result;
        }

    }
}