package ${groupId} {

	import flexunit.framework.TestCase;
	import flexunit.framework.TestSuite;

	public class TestApp extends TestCase {

		/**
		 * Tests our greeting() method
		 */
		public function testGreeting():void {
			var name:String = "Buck Rogers";
			var expectedGreeting:String = "Hello, Buck Rogers";

			var result:String = App.greeting(name);
			assertEquals("Greeting is incorrect", expectedGreeting, result);
		}
	}
}
