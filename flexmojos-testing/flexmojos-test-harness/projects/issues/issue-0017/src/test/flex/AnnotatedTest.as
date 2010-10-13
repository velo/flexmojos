/**
 * @author Seven
 */
package  {

	import flexunit.framework.Assert;
	import flash.errors.IOError;
	import main;
	
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
		
		[Test] 
		public function hiTest():void { 
		   Assert.assertEquals("hi", main.hi() ); 
		}
	}

}