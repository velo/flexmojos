package com.adobe.example
{
	import flexunit.framework.TestCase;
	import com.adobe.example.Calculator;
	
	public class TestCalculator2 extends TestCase
	{

		public function testPass() : void
		{
			assertEquals( 8, 8 );
		}

		public function testFail() : void
		{
			assertEquals( 10, 8 );
		}

		public function testError() : void
		{
			throw new Error("An second error");
		}

	}
}