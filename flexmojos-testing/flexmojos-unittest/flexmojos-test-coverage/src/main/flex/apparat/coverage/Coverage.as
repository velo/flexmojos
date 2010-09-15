package apparat.coverage
{
	import org.sonatype.flexmojos.coverage.CoverageDataCollector;
	import org.sonatype.flexmojos.test.report.TestCoverageReport;
	import mx.collections.ArrayCollection;

	public class Coverage
	{

		private static var cache:Object=new Object();

		public static function onSample(file:String, line:int):void
		{
			if (cache[file] == null)
			{
				cache[file]=new ArrayCollection();
				ArrayCollection(cache[file]).addItem(line);
			}
			else
			{
				if (ArrayCollection(cache[file]).contains(line))
				{
					return;
				}
				else
				{
					ArrayCollection(cache[file]).addItem(line);
				}
			}

			CoverageDataCollector.collect(file, line);
		}
	}
}
