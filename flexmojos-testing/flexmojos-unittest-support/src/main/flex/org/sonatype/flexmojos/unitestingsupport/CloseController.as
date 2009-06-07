/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.unitestingsupport
{
	
	/**
	 * Singleton class to help synchronization  when tests finished. 
	 */
	
	public class CloseController
	{
		
		private var _canClose:Boolean;

		private static var instance:CloseController;
				
		public function set canClose(close:Boolean):void {
			
			_canClose = close;
			
			trace ("Property canClose set to " + _canClose);
		}
		
		public function get canClose():Boolean {
		
			trace ("Property canClose is " + _canClose);
			
			return _canClose;
		}
		
		
		public static function getInstance():CloseController {
			if(instance == null) {
				instance = new CloseController();
				instance.canClose = false;
			}
			return instance;
		}

	}
}