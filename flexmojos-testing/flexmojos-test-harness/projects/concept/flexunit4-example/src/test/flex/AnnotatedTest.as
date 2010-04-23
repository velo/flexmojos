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
/**
 * @author Seven
 */
package  {

	import flexunit.framework.Assert;
	import flash.errors.IOError;
	
	public class AnnotatedTest {
		
		[Test]
		public function addition():void { 
		   Assert.assertEquals(12, 7 + 5); 
		}
		
		[Test(expects="flash.errors.IOError")] 
		public function doIOError():void { 
		   //a test which causes an IOError }Or
		   throw new IOError(); 
		}
	}

}