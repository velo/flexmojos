package apparat.coverage
{
    import org.sonatype.flexmojos.coverage.CoverageDataCollector;


    public class Coverage
    {
        public static function onSample( file:String, line:int ):void
        {
            CoverageDataCollector.collect( file, line );
        }
    }
}
