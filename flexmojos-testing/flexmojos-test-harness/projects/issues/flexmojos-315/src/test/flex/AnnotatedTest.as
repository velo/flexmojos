/**
 * @author Seven
 */
package  {

	import flexunit.framework.Assert;
	import flash.errors.IOError;
	
	public class AnnotatedTest {

        [Embed(source="/shortlogo.png")]
        public var shortlogo:Class;

        [Embed(source="/logo.png")]
        public var logo:Class;
		
		[Test]
		public function embedFromMain():void { 
		   Assert.assertNotNull(shortlogo); 
		}
		
		[Test] 
		public function embedFromTest():void { 
		   Assert.assertNotNull(logo); 
		}
	}

}