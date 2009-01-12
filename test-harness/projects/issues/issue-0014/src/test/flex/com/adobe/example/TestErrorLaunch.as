package com.adobe.example
{
	import flexunit.framework.TestCase;
	
	public class TestErrorLaunch extends TestCase
	{
		
		/**
		 * Constructor.
		 * @param methodName the name of the individual test to run.
		 */
		public function TestErrorLaunch( methodName : String = null )
		{
			super( methodName );
		}
		
		public function testError() : void 
		{
			 throw new Error();
		}

	}
}