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
	import flash.display.DisplayObjectContainer;
	import flash.events.Event;

	public class VUISlider extends UISlider
	{
		
		
		/**
		 * Constructor
		 * @param parent The parent DisplayObjectContainer on which to add this VUISlider.
		 * @param xpos The x position to place this component.
		 * @param ypos The y position to place this component.
		 * @param label The string to use as the label for this component.
		 * @param defaultHandler The event handling function to handle the default event for this component.
		 */
		public function VUISlider(parent:DisplayObjectContainer = null, x:Number = 0, y:Number = 0, label:String = "", defaultEventHandler:Function = null)
		{
			_sliderClass = VSlider;
			super(parent, x, y, label, defaultEventHandler);
		}
		
		/**
		 * Initializes this component.
		 */
		protected override function init():void
		{
			super.init();
			setSize(20, 146);
		}
		
		
		
		
		///////////////////////////////////
		// public methods
		///////////////////////////////////
		
		override public function draw():void
		{
			super.draw();
			_label.x = width / 2 - _label.width / 2;
			
			_slider.x = width / 2 - _slider.width / 2;
			_slider.y = _label.height + 5;
			_slider.height = height - _label.height - _valueLabel.height - 10;
			
			_valueLabel.x = width / 2 - _valueLabel.width / 2;
			_valueLabel.y = _slider.y + _slider.height + 5;
		}
		
		override protected function positionLabel():void
		{
			_valueLabel.x = width / 2 - _valueLabel.width / 2;
		}
		
		
		
		
		///////////////////////////////////
		// event handlers
		///////////////////////////////////
		
		///////////////////////////////////
		// getter/setters
		///////////////////////////////////
		
		override public function get width():Number
		{
			if(_label == null) return _width;
			return Math.max(_width, _label.width);
		}
		
	}
}