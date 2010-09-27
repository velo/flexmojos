/**
 * @author Seven
 */
package  {

	import flexunit.framework.Assert;
	import flash.errors.IOError;
	
	public class AnnotatedTest {
		
		[Test]
		public function addition():void { 
		   Assert.assertEquals(12, 7 + 5); 
		}
		
		[Test(expects="flash.errors.IOError")] 
		public function doIOError():void { 
		   //a test which causes an IOError }Or
		   throw new IOError(); 
		}
	}

}