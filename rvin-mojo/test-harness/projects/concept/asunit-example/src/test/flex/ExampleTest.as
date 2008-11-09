package {
	import asunit.framework.TestCase;

	public class ExampleTest extends TestCase {

		/**
	 	 * Test of whether or not class properly instantiated
	 	 */
	 	public function testInstantiated():void {
	 		assertTrue("Example instantiated", true);
	 	}

		/**
	 	 * Test that is born to lose.
	 	 */
	 	public function testFail():void {
	 		assertFalse("failing test", false);
	 	}

		/**
	 	 * Test the addition method on example
	 	 */
	 	public function testAddition():void {
	 		assertEquals( "Expected:5", 5, 2 + 3 );
	 	}
	}
}