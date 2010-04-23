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
package org.sonatype.mather 
{
	
import org.sonatype.mather.Mather;
import flexunit.framework.Assert;
import flexunit.framework.TestCase;
	
	public class MatherTest extends TestCase
	{
		
		public function testAdd():void {
			var result:Number = Mather.add(1, 2, 3, 4, 5);
			Assert.assertEquals(15, result);
		}

		public function testAddString():void {
			try{
				Mather.add(1, "a", "b", 4, 5);
				Assert.fail("Should throw error");
			} catch (e:Error) {
				//expected
			}
		}
		
	}

}
