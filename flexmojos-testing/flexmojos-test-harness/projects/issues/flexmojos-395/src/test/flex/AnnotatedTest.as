/**
 * @author Seven
 */
package  {

	import flexunit.framework.Assert;
	import org.sonatype.flexmojos.l10n.Resource;
	
	public class AnnotatedTest {
		
		[Test]
		public function addition():void { 
		   Assert.assertNotNull(new Resource().title); 
		}

	}

}