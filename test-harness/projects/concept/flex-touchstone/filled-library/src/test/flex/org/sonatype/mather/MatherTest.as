package org.sonatype.mather 
{
	
import org.sonatype.mather.Mather;
import flexunit.framework.Assert;
import flexunit.framework.TestCase;
	
	public class MatherTest extends TestCase
	{
		
		public function testAdd():void {
			var result:Number = Mather.add(1, 2, 3, 4, 5);
			Assert.assertEquals(15, result);
		}

		public function testAddString():void {
			try{
				Mather.add(1, "a", "b", 4, 5);
				Assert.fail("Should throw error");
			} catch (e:Error) {
				//expected
			}
		}
		
	}

}
