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
 
package com.bit101.components
{
	import flash.display.DisplayObject;
	import flash.display.DisplayObjectContainer;
	import flash.events.Event;

	public class VBox extends Component
	{
		private var _spacing:Number = 5;
		
		
		/**
		 * Constructor
		 * @param parent The parent DisplayObjectContainer on which to add this PushButton.
		 * @param xpos The x position to place this component.
		 * @param ypos The y position to place this component.
		 */
		public function VBox(parent:DisplayObjectContainer = null, xpos:Number = 0, ypos:Number =  0)
		{
			super(parent, xpos, ypos);
		}
		
		/**
		 * Override of addChild to force layout;
		 */
		override public function addChild(child:DisplayObject) : DisplayObject
		{
			super.addChild(child);
			child.addEventListener(Event.RESIZE, onResize);
			invalidate();
			return child;
		}
		
		/**
		 * Internal handler for resize event of any attached component. Will redo the layout based on new size.
		 */
		protected function onResize(event:Event):void
		{
			invalidate();
		}
		
		/**
		 * Draws the visual ui of the component, in this case, laying out the sub components.
		 */
		override public function draw() : void
		{
			var ypos:Number = 0;
			for(var i:int = 0; i < numChildren; i++)
			{
				var child:DisplayObject = getChildAt(i);
				child.y = ypos;
				ypos += child.height;
				ypos += _spacing;
			}
		}
		
		/**
		 * Gets / sets the spacing between each sub component.
		 */
		public function set spacing(s:Number):void
		{
			_spacing = s;
			invalidate();
		}
		public function get spacing():Number
		{
			return _spacing;
		}
	}
}