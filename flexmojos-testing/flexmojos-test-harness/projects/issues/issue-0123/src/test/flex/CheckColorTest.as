/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
