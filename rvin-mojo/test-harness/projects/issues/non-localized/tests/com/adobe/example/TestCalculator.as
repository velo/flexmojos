package com.adobe.example
{
	import flexunit.framework.TestCase;
	import com.adobe.example.Calculator;
	
	public class TestCalculator extends TestCase
	{
		private var calculator : Calculator;
		
		/**
		 * Constructor.
		 * @param methodName the name of the individual test to run.
		 */
		public function TestCalculator( methodName : String = null )
		{
			super( methodName );
		}
	
		/**
		 * @see flexunit.framework.TestCase#setUp().
		 */
		override public function setUp() : void
		{
			trace("setUp")
			calculator = new Calculator();
		}
		
		/**
		 * @see flexunit.framework.TestCase#tearDown().
		 */
		override public function tearDown() : void
		{
			trace("tearDown");
			calculator = null;
		}

		public function testMultiplyPass() : void
		{
			trace("testMultiplyPass");
			var result : Number = calculator.multiply( 2, 4 );
			assertEquals( 8, result );
		}

	}
}