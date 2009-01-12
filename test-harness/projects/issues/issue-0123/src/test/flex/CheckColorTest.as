package 
{
	import flash.display.DisplayObject;
	
	import flexunit.framework.Assert;
	import flexunit.framework.TestCase;
	
	import mx.controls.*;
	import mx.core.*;

	import Main;


	public class CheckColorTest extends TestCase
	{

		public function testSomething():void {
			var main:Main = Main(Application.application);
			var myButton:Button = main.myButton;
			
			var color:int = int( myButton.getStyle("color") );
			Assert.assertEquals(0xFF0000, color);
	    }

	}
}
