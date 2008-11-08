package info.flexmojos.unitestingsupport
{
	import flash.utils.getDefinitionByName;
	
	import funit.core.FUnitFramework;
	
	import info.flexmojos.unitestingsupport.flexunit.FlexUnitListener;
	
	import mx.core.Application;
	import mx.events.FlexEvent;
	
	public class TestApplication extends Application
	{
		
		private var tests:Array;
		
		public function set port(port:int):void {
			SocketReporter.port = port;
		}
		
		public function TestApplication()
		{
			tests = new Array();
			
			addEventListener(FlexEvent.CREATION_COMPLETE, runTests);
		}
		
		private function runTests(e:*):void 
		{
			SocketReporter.totalTestCount = tests.length;
			var testsScheduledToRun:int = 0;
			
			//flexunit supported
			if(getDefinitionByName("flexunit.framework.Test"))
			{
				testsScheduledToRun = FlexUnitListener.run(tests);
			}

			//funit supported			
			if(getDefinitionByName("funit.core.FUnitFramework"))
			{
				
			}
			
			//fluint supported
			if(getDefinitionByName("net.digitalprimates.fluint.tests.TestCase"))
			{
				
			}
		}

		/**
		 * Test to be run
		 * @param test the Test to run.
		 */
		public function addTest( test:Class ) : void
    	{
    	    tests.push(test);
		}
	}
}